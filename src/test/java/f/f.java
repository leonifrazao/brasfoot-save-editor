package f;

import br.com.saveeditor.brasfoot.application.services.CalendarManagementServiceTest.Competition;
import br.com.saveeditor.brasfoot.application.services.CalendarManagementServiceTest.CountryData;

public class f extends Competition {
    private final CountryData country;

    public f(String name, int countryId) {
        super(name, 2);
        country = new CountryData(countryId);
    }

    public CountryData yg() { return country; }
}
