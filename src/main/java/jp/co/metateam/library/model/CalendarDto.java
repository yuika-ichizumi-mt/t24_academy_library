package jp.co.metateam.library.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

    
@Getter
@Setter
public class CalendarDto {

    private String title;

    private Long id; 

    private String availableStockCount;

    private List<StockListDto> stockList;
    
}
