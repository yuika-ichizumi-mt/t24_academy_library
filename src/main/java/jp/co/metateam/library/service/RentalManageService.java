package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;

@Service
public class RentalManageService {

    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;

     @Autowired
    public RentalManageService(
        AccountRepository accountRepository,
        RentalManageRepository rentalManageRepository,
        StockRepository stockRepository
    ) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List <RentalManage> findAll() {
        List <RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }

    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }

    @Transactional 
    public void save(RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            RentalManage rentalManage = new RentalManage();
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());  //更新された日時が分かる
        
        if (status == RentalStatus.RENTAlING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }

        return rentalManage;
    }




@Transactional  
/*
トランザクションが正常にコミットされれば、すべての変更がデータベースに反映される。
ただし、トランザクション内で例外がスローされると、変更内容は取り消される    
*/
public void update(Long id, RentalManageDto rentalManageDto) throws Exception {
    //スロー宣言を行うことで、try-catch文がいらない
    try {    
        //this.accountRepositoryは同じクラス内にあるからthisで持ってくることができる
        //選択された社員番号の情報をアカウントテーブルから取得する
        //nullはアカウントテーブルに情報がなくて、取得できなかったという意味
        RentalManage updateTargetRental= this.rentalManageRepository.findById(id).orElse(null);
        if (updateTargetRental == null) {
            throw new Exception("REntalManage record not found.");
             
          //this.renrtalRepositoryは同じクラス内にあるからthisで持ってくることができる
        }

        Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
        //orElse(null) は、結果が存在しない場合に、デフォルトの値として null を返す
        if (account == null) {
            throw new Exception("Account not found.");
        }
         
       //this.stockRepositoryは同じクラス内にあるからthisで持ってくることができる
        Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
        if (stock == null) {
            throw new Exception("Stock not found.");
        }

        updateTargetRental.setId(rentalManageDto.getId());
        updateTargetRental.setAccount(account);
        updateTargetRental.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
        updateTargetRental.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
        updateTargetRental.setStatus(rentalManageDto.getStatus());
        updateTargetRental.setStock(stock);

        // データベースへの保存
        this.rentalManageRepository.save(updateTargetRental);
    } catch (Exception e) {
        throw e;

       
    }
     //Integerとはプログラミング言語 などで用いられる データ型 の一つで、整数の値を格納できるもの。
//isStatusErrorでbeforeStatusと afterStatusのステータスを取得  
    }
    public String isStatusError(Integer beforeStatus,Integer afterStatus,LocalDate newexpectedRentalOn,LocalDate currentDate) {  //引数＝使う変数（オブジェクト）
        //返却済み・キャンセルのステータスは変更できない   //integer→rental statusの中にある
         // == は比較演算子の等しいという意味
        //  a || b  aまたはbがtrueのとき true
        //返却済みと、キャンセルのステータスは変更できない　//getValueなのはprivateであるため
        if(beforeStatus == RentalStatus.RETURNED.getValue() || beforeStatus == RentalStatus.CANCELED.getValue()) {
            return "このステータスから変更することはできません";
            //貸出待ちから返却済みに変更できない
            
        //貸出待ちから返却済みに変更できない
        } else if(beforeStatus == RentalStatus.RENT_WAIT.getValue() && afterStatus == RentalStatus.RETURNED.getValue()){
            return "このステータスは無効です";
        //貸出中から貸出待ちに変更できない
        } else if(beforeStatus == RentalStatus.RENTAlING.getValue() && afterStatus == RentalStatus.RENT_WAIT.getValue()) {
            return "このステータスは無効です";
        //貸出中からキャンセルに変更できない
        } else if(beforeStatus == RentalStatus.RENTAlING.getValue() && afterStatus == RentalStatus.CANCELED.getValue()){
            return "このステータスは無効です";
        //貸出待ちから貸出中に変更するのは、貸出予定日当日である必要がある
        } else if(!newexpectedRentalOn.isEqual(currentDate) && beforeStatus == RentalStatus.RENT_WAIT.getValue() && afterStatus == RentalStatus.RENTAlING.getValue()){
            return "貸出予定日以外の日に、貸出中に変更することはできません";
        }
        
        
            return null;
        
      
    }
}
