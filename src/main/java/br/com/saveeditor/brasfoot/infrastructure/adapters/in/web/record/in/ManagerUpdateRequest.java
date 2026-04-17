package br.com.saveeditor.brasfoot.adapters.in.web.record.in;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ManagerUpdateRequest {
    private String name;
    private Integer confidenceBoard;
    private Integer confidenceFans;

    @Min(15)
    @Max(100)

    public ManagerUpdateRequest() {}

}
