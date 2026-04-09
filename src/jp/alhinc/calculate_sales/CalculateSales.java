package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();

		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//コマンドライン引数で指定したディレクトリーのファイルをフルパスで配列に格納
		File[] files = new File(args[0]).listFiles();

		//売上ファイルを保持するList
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {

			String regex = "^\\d{8}\\.rcd$"; //数字8桁の後「.rcd」の拡張子で終わることを表す正規表現

			//files[i].getName() でファイル名が取得できます。
			if (files[i].getName().matches(regex)) {
				rcdFiles.add(files[i]);
			}
		}

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for(int i = 0; i < rcdFiles.size(); i++) {

			//支店コード、売上を保持するList
			List<String> branchCodeSalesList = new ArrayList<>();

			//branchCodeSalesListのインデックス指定用
			final int BRANCH_CODE_INDEX = 0; // 支店コード
			final int SALES_INDEX = 1; // 売上金額

			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
			if(!readSalesFile(rcdFiles.get(i),  branchNames, branchSales, branchCodeSalesList)) {
				return;
			}

			//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
			long fileSale = Long.parseLong(branchCodeSalesList.get(SALES_INDEX));

			//支店名を格納
			String branchCode = branchCodeSalesList.get(BRANCH_CODE_INDEX);

			//読み込んだ売上⾦額を加算します。
			Long saleAmount = branchSales.get(branchCode) + fileSale;

			//加算した売上⾦額をMapに追加します。
			branchSales.put(branchCode,saleAmount);
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
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
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				// 文字列を「,」で分割して文字列型の配列に格納
				String[] items = line.split(",");
				// items[0] 支店コード
				// items[1] 支店名
			    branchNames.put(items[0], items[1]);
			    branchSales.put(items[0], 0L);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 売上ファイル読み込み処理
	 *
	 * @param フォルダパス+ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readSalesFile(File salesFilePath, Map<String, String> branchNames, Map<String, Long> branchSales, List<String> branchCodeSalesList) {
		BufferedReader br = null;

		try {
			FileReader fr = new FileReader(salesFilePath);
			br = new BufferedReader(fr);

			String branchcode;
			String sales;

			// ファイルの中身が固定で2行のため、上から順に読み込む
			branchcode = br.readLine();
			sales = br.readLine();

			branchCodeSalesList.add(branchcode);
			branchCodeSalesList.add(sales);

		} catch(IOException e){
				//正常系の実装が完了してから着手
		} finally {
			//ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e){
					//正常系の実装が完了してから着手
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
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (String key : branchNames.keySet()) {
				//keyという変数には、Mapから取得したキーが代入されています。
				//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
				//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}

		} catch(IOException e){
			//正常系の実装が完了してから着手
		} finally {
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					//正常系の実装が完了してから着手
				}
			}
		}
		return true;
	}
}
