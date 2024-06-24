package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import jp.co.metateam.library.model.BookMstDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.RentalManageRepository;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,
            RentalManageRepository rentalManageRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional // bookmstrepository
    public List<BookMst> findAllBookData() {
        List<BookMst> findAllBookData = this.bookMstRepository.findAllBookData();
        return findAllBookData; // バグを防ぐ（情報が多いから）
    }

    @Transactional // rentalmanagerepository
    public Long scheduledRentaWaitData(Date day, List<String> stock_id) {
        return this.rentalManageRepository.scheduledRentaWaitData(day, stock_id);
    }

    @Transactional // rentalmanagerepository
    public Long scheduledRentalingData(Date day, List<String> stock_id) {
        return this.rentalManageRepository.scheduledRentalingData(day, stock_id);
    }

    @Transactional // stockrepository
    public List<Stock> lendableBook(Date choiceDate, Long id) {
        return this.stockRepository.lendableBook(choiceDate, id);
    }

    @Transactional // stockrepository
    public List<Stock> findAllAvailableStockData(Long book_id) {
        return this.stockRepository.findAllAvailableStockData(book_id);
    }

    @Transactional
    public List<Stock> bookStockAvailable(Long id){
        return this.stockRepository.bookStockAvailable(id);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<List<String>> generateValues(Integer year, Integer month, Integer daysInMonth) {
        List<List<String>> bigValues = new ArrayList<>();
        List<BookMst> bookData = findAllBookData(); // transactionalでは呼びだしただけだからここでも呼び出す必要がある

        // 書籍分ループ（：拡張ループ）
        for (BookMst bookLoop : bookData) {
            //書籍名、利用可能在庫数、日毎の利用可能在庫数
            List<String> values = new ArrayList<>();
            values.add(bookLoop.getTitle()); // 日毎の利用可能在庫数もカウント出来ている

            // 利用可能総在庫数
            List<Stock> availableList = findAllAvailableStockData(bookLoop.getId());
            // 数字を文字列に変換
            String availableStocksCount = String.valueOf(availableList.size());
            values.add(availableStocksCount);

            List<String> stockIdList = new ArrayList<>();
            for (Stock stock : availableList) {
                // stockIdって箱にループしてきたIdをつめてる
                stockIdList.add(stock.getId());

            }

             //現在日付を取得
             LocalDate today = LocalDate.now();

            // 日付分ループ
            for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) { 
                LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
                // LocalDate型のlocalDateををDate型に変換しdateに入れる
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                // 日ごとの利用可能在庫数
                Long scheduledRentaWaitDataCount = scheduledRentaWaitData(date, stockIdList);
                Long scheduledRentalingDataCount = scheduledRentalingData(date, stockIdList);

                Long total = availableList.size() - (scheduledRentaWaitDataCount + scheduledRentalingDataCount);

                // 計算してtotalに入れたデータを String型のtotalValueに変換
                String totalValue = (total <= 0) ? "×" :Long.toString(total);

                values.add(totalValue);
               
                // List<Stock> availabStock = findAllAvailableStockData(null);
                LocalDate currentDateOfMonth = LocalDate.of(year, month, dayOfMonth);
                // 過去日の判定
                if (today != null && currentDateOfMonth.isBefore(today)) {
                    values.add("×");
                    continue; // 次の日付へ
                }
                
            }

            bigValues.add(values);
        }
        return bigValues;
    }

    //遷移後
    public List<Stock> availableStockValues(java.util.Date choiceDate, Integer title) {

        //書籍IDを取得
        Long id = Long.valueOf(title + 1);  //titleは書籍ID　ⅮBeaverが0からスタートしているから

        List<Stock> availableList = lendableBook(choiceDate,id);
        List<Stock> StockAvailable = this.stockRepository.bookStockAvailable(id);

        StockAvailable.removeAll(availableList); //removeAllによって表示できる在庫管理番号を厳選

        return StockAvailable;
    }
}

// // FIXME ここで各書籍毎の日々の在庫を生成する処理を実装する
// // FIXME ランダムに値を返却するサンプルを実装している
// String[] stockNum = {"1", "2", "3", "4", "×"};
// Random rnd = new Random();
// List<String> values = new ArrayList<>();
// values.add("スッキリわかるJava入門 第4版"); // 対象の書籍名
// values.add("10"); // 対象書籍の在庫総数

// for (int i = 1; i <= daysInMonth; i++) {
// int index = rnd.nextInt(stockNum.length);
// values.add(stockNum[index]);
// }
// return values;
// }
