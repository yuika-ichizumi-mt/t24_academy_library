package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

	Optional<Stock> findById(String id);
    
    List<Stock> findByBookMstIdAndStatus(Long book_id,Integer status);

    //利用可能総在庫数をカウントする
    @Query("SELECT s FROM Stock s WHERE s.status = 0 AND s.bookMst.id = ?1 AND s.deletedAt IS NULL")
      List<Stock> findAllAvailableStockData(Long book_id);

    //遷移後

    //選択した日付が貸出予定日と返却予定日の期間に被っている在庫管理番号を取ってくる
    @Query("SELECT DISTINCT s " + "FROM Stock s " + "LEFT OUTER JOIN RentalManage rm ON s.id = rm.stock.id " +
             "WHERE ?1 BETWEEN rm.expectedRentalOn AND rm.expectedReturnOn " + "AND s.bookMst.id = ?2 " + "AND s.status = 0 " + "AND deletedAt IS NULL")
      List<Stock> lendableBook(Date choiceDate, Long id);

    //プルダウンに表示するための在庫管理番号を取得
    @Query("SELECT s " + "FROM Stock s " + "WHERE s.status = 0 " + "AND s.bookMst.id = ?1 " + "AND deletedAt IS NULL")
      List<Stock> bookStockAvailable(Long id);

}

    