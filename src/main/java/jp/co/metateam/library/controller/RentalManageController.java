package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Date;

import jakarta.validation.Valid;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.Account;



/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得

        List <RentalManage> rentalManageList = this.rentalManageService.findAll();


        // 貸出一覧画面に渡すデータをmodelに追加

        model.addAttribute("rentalManageList", rentalManageList);

        // 貸出一覧画面に遷移
        return "rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model) {
        
        List <Stock> stockList = this.stockService.findAll();
        List<Account> accounts = this.accountService.findAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }


    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        }
    }


@GetMapping("/rental/{id}/edit")
public String edit(@PathVariable("id") Long id, Model model) {
    List <Stock> stockList = this.stockService.findAll();  //在庫管理番号のプルダウンリスト作成
    List <Account> accounts = this.accountService.findAll(); //社員番号のプルダウンリスト作成
 
        model.addAttribute("stockList", stockList); //在庫管理番号のリストを表示（プルダウン）
        model.addAttribute("accounts", accounts);  //社員番号のリストを表示（プルダウン）
        model.addAttribute("rentalStatus", RentalStatus.values());  //貸出ステータスリスト（プルダウン）
 
        RentalManage rentalManage = this.rentalManageService.findById(id); //貸出管理テーブルから{id}の情報を持ってくる
 
        /*
         * 取得した貸出管理情報をそれぞれセットする
         */
        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
 
        rentalManageDto.setId(rentalManage.getId());
        rentalManageDto.setStatus(rentalManage.getStatus());
        rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
        rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
        rentalManageDto.setStockId(rentalManage.getStock().getId());
        rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
 
        /*
         * セットした内容の表示
         */
        model.addAttribute("rentalManageDto", rentalManageDto);
    }
 
    return "rental/edit";
 }



@PostMapping("/rental/{id}/edit")
public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto,BindingResult result, Model model) {
    //@PathVariable("id") String id = ("/rental/{id}/edit")のURLに含まれる動的な引数id(貸出管理番号)を受け取る
        //@Valid @ModelAttribute RentalManageDto rentalManageDto = バリデーションを行う　rentalManageDtoクラスにメソッドまたはメソッドの引数に付与できる。
        //@BindingResult result = バリデーションの処理の結果を格納　エラーがあった場合その情報をresultに保存する
        //Model model = ビューに渡すデータの保持　controllerの中で処理された結果をビューに表示する

    // // 変更前のステータス
    // RentalManage rentalManage = rentalManageService.findById(Long.valueOf(id));
    // int beforeStatus = rentalManage.getStatus();
 
    // // 変更後のステータス
     //Integer afterexpectedRentalOn = rentalManageDto.getStatus();     
    
    //変更後の貸出予定日の取得
     Date afterexpectedRentalOn = rentalManageDto.getExpectedRentalOn();
    //DateをLocalDateに変換
     //LocalDate newexpectedRentalOn = afterexpectedRentalOn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
     //今日の日付の取得
     //LocalDate currentDate = LocalDate.now();
    
    try {   // ifをまとめる箱
              RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id)); 
               //指定されたid(貸出管理番号)に対応するRentalManageオブジェクトを取得
               // Long.valueOf(id) = 文字列からLong型への変換
              String validerror = rentalManageService.isStatusError(rentalManage.getStatus(),rentalManageDto.getStatus(),afterexpectedRentalOn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now());  //エラー
                 
               //rentalManage(変更前)のステータスとrentalManageDto(変更後)のステータスを比較し、エラーがあるかどうかを判定
             if (validerror != null) {  //nullじゃなかった時
                result.addError(new FieldError("rentalManageDto","status",validerror)); 
                 //入力された情報が引っかかる場合result にエラー情報を追加
                // 具体的には、FieldError オブジェクトを作成し、その中にエラーの詳細情報を設定
                // エラー情報は、rentalManageDto の status フィールドに関連付けられている          
            }

             if (result.hasErrors()) {   //
                throw new Exception("Validation error.");
                //例外処理を行っている　これが無いとプログラムエラーが出た時に処理が止まってしまう
            }
            // 更新処理 
            // 登録処理　rentalManageServiceのupdateメソッドを呼び出し、id と rentalManageDto を引数として渡す     
           this. rentalManageService.update(Long.valueOf(id), rentalManageDto);

            return  "redirect:/rental/index";  //リダイレクトの時新しいURLを作る;
            //URL　リダイレクトのときは新しいURLを作る　戻る場合は139行目のまったく同じURL


      }catch (Exception e) {  //tryで何かがバリデーションに引っかかったらcatch（ e 例外を表す変数）
            log.error(e.getMessage());  //プログラムの中で問題が発生したときに例外のエラーメッセージをエラーレベルのログに記録して問題を追跡
            List <Stock> stockList = this.stockService.findAll();  //在庫管理番号のプルダウンリスト作成　　　findAll 条件に合うものをすべて取得

            List <Account> accounts = this.accountService.findAll(); //社員番号のプルダウンリスト作成
              //this 同じクラスの中にある箱を持ってくる
                model.addAttribute("stockList", stockList); //在庫管理番号のリストを表示（プルダウン）
                model.addAttribute("accounts", accounts);  //社員番号のリストを表示（プルダウン）
                model.addAttribute("rentalStatus", RentalStatus.values());  //貸出ステータスリスト（プルダウン）
                //model.addAttribute()はコントローラーからビューにデータを渡す（取得した結果）

            return "rental/edit";//どのテンプレートをもってくるか（貸出編集に遷移）
        }
        
}
}



