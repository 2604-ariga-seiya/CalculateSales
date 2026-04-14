package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String BRANCH_FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String BRANCH_FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String COMMODITY_FILE_NOT_EXIST ="商品定義ファイルが存在しません";
	private static final String COMMODITY_FILE_INVALID_FORMAT ="商品定義ファイルのフォーマットが不正です";
	private static final String FILE_NAME_NOT_SEQUENTIAL = "売上ファイル名が連番になっていません";
	private static final String INVALID_DIGIT_COUNT = "合計金額が10桁を超えました";
	private static final String BRANCH_CODE_NOT_EXIST = "%sの支店コードが不正です";
	private static final String COMMODITY_CODE_NOT_EXIST = "%sの商品コードが不正です";
	private static final String FILE_CONTENTS_INVALID_FORMAT = "%sのフォーマットが不正です";

	// 正規表現
	private static final String BRANCH_CODE_PATTERN = "^\\d{3}$"; //数字3桁
	private static final String COMMODITY_CODE_PATTERN = "^[a-zA-Z0-9]{8}$"; //アルファベットと数字、8桁固定
	private static final String SALES_FILE_NAME_PATTERN = "^\\d{8}\\.rcd$"; //数字8桁の後「.rcd」の拡張子で終わる
	private static final String PRICE_PATTERN = "^[0-9]+$"; //数字だけで構成された文字列

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		// コマンドライン引数が渡されているか判定
		if (args.length != 1) {
			//コマンドライン引数が1つ設定されていなかった場合は、
		    //エラーメッセージをコンソールに表⽰します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();

		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();

		// 商品コードと売上を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, BRANCH_CODE_PATTERN, BRANCH_FILE_NOT_EXIST, BRANCH_FILE_INVALID_FORMAT)) {
			return;
		}

		// 商品定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_COMMODITY_LST, commodityNames, commoditySales, COMMODITY_CODE_PATTERN, COMMODITY_FILE_NOT_EXIST, COMMODITY_FILE_INVALID_FORMAT)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//コマンドライン引数で指定したディレクトリーのファイルをフルパスで配列に格納
		File[] files = new File(args[0]).listFiles();

		//売上ファイルを保持するList
		List<File> rcdFiles = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {

			String regex = SALES_FILE_NAME_PATTERN;

			//files[i].getName() でファイル名が取得できます。
			String fileName = files[i].getName();
			if (files[i].isFile() && fileName.matches(regex)) {
				//売上ファイルを売上ファイルリストに格納
				rcdFiles.add(files[i]);
			}
		}

		//連番チェックを⾏う前に、売上ファイルを保持しているListをソートする
		Collections.sort(rcdFiles);

		//⽐較回数は売上ファイルの数よりも1回少ないため、
		//繰り返し回数は売上ファイルのリストの数よりも1つ⼩さい数です。
		for(int i = 0; i < rcdFiles.size() -1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

		    //⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
			if((latter - former) != 1) {
				//2つのファイル名の数字を⽐較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表⽰します。
				System.out.println(FILE_NAME_NOT_SEQUENTIAL);
			}
		}

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		BufferedReader br = null;
		for (int i = 0; i < rcdFiles.size(); i++) {
			try {
				FileReader fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);

				//支店コード、商品コード、売上を保持するList
				List<String> rcdFilesContentsList = new ArrayList<>();

				String line = "";

				// 一行ずつ読み込む
				while ((line = br.readLine()) != null) {
					//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
					//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
					rcdFilesContentsList.add(line);
				}

				//売上ファイルの中身が2行であるか判定
				if(rcdFilesContentsList.size() != 3) {
					System.out.println(String.format(FILE_CONTENTS_INVALID_FORMAT, rcdFiles.get(i).getName()));
					return;
				}

				//支店コードを格納
				String branchCode = rcdFilesContentsList.get(0);

				//商品コードを格納
				String commodityCode = rcdFilesContentsList.get(1);

				//支店コード、商品コード、売上を保持するListから売上を取得
				String InputSaleAmount = rcdFilesContentsList.get(2);

				//売上ファイルの支店コードが支店定義ファイルに該当するか判定
				if (!branchNames.containsKey(branchCode)) {

				    //⽀店情報を保持しているMapに売上ファイルの⽀店コードが存在しなかった場合は、
				    //エラーメッセージをコンソールに表⽰します。
					System.out.println(String.format(BRANCH_CODE_NOT_EXIST, rcdFiles.get(i).getName()));
					return;
				}

				//売上ファイルの商品コードが商品定義ファイルに該当するか判定
				if (!commodityNames.containsKey(commodityCode)) {

				    //商品情報を保持しているMapに売上ファイルの商品コードが存在しなかった場合は、
				    //エラーメッセージをコンソールに表⽰します。
					System.out.println(String.format(COMMODITY_CODE_NOT_EXIST, rcdFiles.get(i).getName()));
					return;
				}

				//売上ファイルの売上金額が数字であるか判定
				if(!InputSaleAmount.matches(PRICE_PATTERN)) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				long fileSale = Long.parseLong(InputSaleAmount);

				//読み込んだ支店別の売上⾦額を加算します。
				Long branchSaleAmount = branchSales.get(branchCode) + fileSale;

				//読み込んだ商品別の売上⾦額を加算します。
				Long commoditySaleAmount = commoditySales.get(commodityCode) + fileSale;

				//それぞれの合計金額の桁数が10以内であることを判定
				if(commoditySaleAmount >= 10000000000L | branchSaleAmount >= 10000000000L){
					System.out.println(INVALID_DIGIT_COUNT);
					return;
				}

				//加算した支店別の売上⾦額をMapに追加します。
				branchSales.put(branchCode, branchSaleAmount);

				//加算した商品別の売上⾦額をMapに追加します。
				commoditySales.put(commodityCode, commoditySaleAmount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;

			} finally {
				//ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> subjectNames, Map<String, Long> subjectSales, String regex, String notFoundMessage, String validationErrorMessage) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);

			//定義ファイルが存在することを確認
			if(!file.exists()) {
				System.out.println(notFoundMessage);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				// 文字列を「,」で分割して文字列型の配列に格納
				String[] items = line.split(",");
				// items[0] コード
				// items[1] 名称

				if((items.length != 2) || (!items[0].matches(regex))){
				    //⽀店定義ファイルの仕様が満たされていない場合、
				    //エラーメッセージをコンソールに表⽰します。
					System.out.println(validationErrorMessage);
					return false;
				}

				//コード、名称のMapにコードと名称を格納
				subjectNames.put(items[0], items[1]);

				//コード、売上のMapにコードと初期値０を格納
				subjectSales.put(items[0], 0L);
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> subjectNames, Map<String, Long> subjectSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (String key : subjectNames.keySet()) {
				//keyという変数には、Mapから取得したキーが代入されています。
				//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
				//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。

				//「支店コード,支店名,売上」をファイルに書き込み
				bw.write(key + "," + subjectNames.get(key) + "," + subjectSales.get(key));

				//改行
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;

		} finally {
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}
}
