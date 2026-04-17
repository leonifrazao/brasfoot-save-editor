package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.application.shared.NavegacaoState;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({ "rawtypes" })
public class SaveFileService {
    private final Kryo kryoReader; // Apenas para leitura

    public SaveFileService() {
        this.kryoReader = new Kryo();
        configurarKryoParaLeitura(this.kryoReader);
    }

    private void configurarKryoParaLeitura(Kryo kryo) {
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setRegistrationRequired(false);
        // Use ContextClassLoader to find classes in Fat JAR
        kryo.setClassLoader(Thread.currentThread().getContextClassLoader());

        // Serializer especial para ArrayList (apenas para leitura)
        CollectionSerializer arrayListSerializer = new CollectionSerializer() {
            @Override
            protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
                return new ArrayList();
            }
        };
        kryo.register(ArrayList.class, arrayListSerializer);
    }

    /**
     * Cria um Kryo LIMPO apenas para escrita.
     * Não usa as configurações especiais de leitura que podem corromper o arquivo.
     */
    private Kryo criarKryoParaEscrita() {
        Kryo kryoWriter = new Kryo();
        kryoWriter.setRegistrationRequired(false);
        kryoWriter.setClassLoader(Thread.currentThread().getContextClassLoader());
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
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create snapshot", e);
        }
    }

    /**
     * Deserializes a byte array back into a NavegacaoState.
     */
    public NavegacaoState restoreFromSnapshot(byte[] snapshot, String originalPath) {
        try (Input input = new Input(new java.io.ByteArrayInputStream(snapshot))) {
            Object objetoRaiz = kryoReader.readClassAndObject(input);
            Object dataAfQ = kryoReader.readClassAndObject(input);
            return new NavegacaoState(objetoRaiz, dataAfQ, originalPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore snapshot", e);
        }
    }
}