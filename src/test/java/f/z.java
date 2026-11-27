package f;

import br.com.saveeditor.brasfoot.application.services.CalendarManagementServiceTest.Match;
import br.com.saveeditor.brasfoot.application.services.CalendarManagementServiceTest.Stage;
import java.util.ArrayList;
import java.util.List;

public class z extends Stage {
    public final List<Round> rounds = new ArrayList<>();
    public int phaseCode = -1;

    public List<Round> zp() { return rounds; }
    public int zf() { return phaseCode; }

    public static class Round {
        public final List<Integer> as;
        public final List<Match> first = new ArrayList<>();
        public final List<Match> second = new ArrayList<>();
        private final int number;
        private final int size;
        private final int format;
        public int w = 2;

        public Round(int number, int size, int format, Integer... dayIndices) {
            this.number = number;
            this.size = size;
            this.format = format;
            this.as = new ArrayList<>(List.of(dayIndices));
        }

        public List<Match> zW() { return first; }
        public List<Match> zX() { return second; }
        public int lU() { return number; }
        public int zU() { return size; }
        public int ze() { return format; }
    }
}
