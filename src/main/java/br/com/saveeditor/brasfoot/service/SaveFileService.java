package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.application.ports.out.BrasfootGameLibraryPort;
import br.com.saveeditor.brasfoot.domain.NavegacaoState;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({ "rawtypes" })
public class SaveFileService {
    private final BrasfootGameLibraryPort gameLibraryPort;
    private final BrasfootSaveCompatibility saveCompatibility;

    public SaveFileService(BrasfootGameLibraryPort gameLibraryPort, BrasfootSaveCompatibility saveCompatibility) {
        this.gameLibraryPort = gameLibraryPort;
        this.saveCompatibility = saveCompatibility;
    }

    private Kryo criarKryoParaLeitura() {
        Kryo kryoReader = new Kryo();
        configurarKryoParaLeitura(kryoReader);
        return kryoReader;
    }

    private void configurarKryoParaLeitura(Kryo kryo) {
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setRegistrationRequired(false);
        kryo.setClassLoader(gameLibraryPort.getClassLoader());

        // Serializer especial para ArrayList (apenas para leitura)
        CollectionSerializer arrayListSerializer = new CollectionSerializer() {
            @Override
            protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
                return new ArrayList();
            }
        };
        kryo.register(ArrayList.class, arrayListSerializer);

        // HashSet inicializa um HashMap interno no construtor. A estrategia
        // Objenesis usada para as classes do Brasfoot pula esse construtor.
        CollectionSerializer hashSetSerializer = new CollectionSerializer() {
            @Override
            protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
                return new HashSet();
            }
        };
        kryo.register(HashSet.class, hashSetSerializer);
    }

    /**
     * Cria um Kryo LIMPO apenas para escrita.
     * Não usa as configurações especiais de leitura que podem corromper o arquivo.
     */
    private Kryo criarKryoParaEscrita() {
        Kryo kryoWriter = new Kryo();
        kryoWriter.setRegistrationRequired(false);
        kryoWriter.setClassLoader(gameLibraryPort.getClassLoader());
        // NÃO configura InstantiatorStrategy nem CollectionSerializer customizado
        return kryoWriter;
    }

    /**
     * Serializes the state to a byte array (Snapshot).
     */
    public byte[] createSnapshot(NavegacaoState state) {
        Kryo kryoWriter = criarKryoParaEscrita();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (Output output = new Output(baos)) {
            kryoWriter.writeClassAndObject(output, state.getObjetoRaiz());
            kryoWriter.writeClassAndObject(output, state.getDataAfQ());
            output.flush();
            return saveCompatibility.encode(baos.toByteArray(), state.isCompressedSave());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create snapshot", e);
        }
    }

    /**
     * Deserializes a byte array back into a NavegacaoState.
     */
    public NavegacaoState restoreFromSnapshot(byte[] snapshot, String originalPath) {
        Kryo kryoReader = criarKryoParaLeitura();
        BrasfootSaveCompatibility.DecodedSave decoded = saveCompatibility.decode(snapshot);
        try (Input input = new Input(new java.io.ByteArrayInputStream(decoded.payload()))) {
            Object objetoRaiz = kryoReader.readClassAndObject(input);
            Object dataAfQ = kryoReader.readClassAndObject(input);
            return new NavegacaoState(objetoRaiz, dataAfQ, originalPath, decoded.gzip());
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore snapshot", e);
        }
    }
}
