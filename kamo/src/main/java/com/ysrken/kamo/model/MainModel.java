package com.ysrken.kamo.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ysrken.kamo.Constant;
import com.ysrken.kamo.controller.BattleSceneReflectionController;
import com.ysrken.kamo.controller.SceneHelperController;
import com.ysrken.kamo.controller.TimerController;
import com.ysrken.kamo.service.*;
import com.ysrken.kamo.stage.ExtraStage;
import com.ysrken.kamo.stage.ExtraStageFactory;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * メイン画面のModel
 * @author ysrken
 */
@Component
public class MainModel {
	/**
	 * スクリーンショットボタンを押せないか？
	 */
	@Getter
    private BooleanProperty disableSaveScreenshotFlg = new SimpleBooleanProperty(true);
	
	/**
	 * 各種画面を開いているか？
	 */
	@Getter
	private BooleanProperty openBattleSceneReflectionFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty openTimerFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty openFleetCombineFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty openSceneHelperFlg = new SimpleBooleanProperty(false);
	
	/**
	 * 各種設定項目
	 */
	@Getter
	private BooleanProperty autoGetPositionFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty disableCheckMovedPositionFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty blindNameTextFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty specialGetPosFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty saveWindowPositionFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty marchBlockerFlg = new SimpleBooleanProperty(false);

	@Getter
	private BooleanProperty updateFps01Flg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty updateFps03Flg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty updateFps05Flg = new SimpleBooleanProperty(true);
	@Getter
	private BooleanProperty updateFps10Flg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty updateFps15Flg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty updateFps30Flg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty updateFps60Flg = new SimpleBooleanProperty(false);

	/**
	 * シーン情報
	 */
	@Getter
	private StringProperty nowSceneText = new SimpleStringProperty("シーン判定：[不明]");
	
	/**
	 * 動作ログ情報
	 */
	@Getter
	private StringProperty messageLogText = new SimpleStringProperty("");

	/**
	 * 各種画面
	 */
	private ExtraStage battleSceneReflectionStage = null;
	private ExtraStage timerStage = null;
	private ExtraStage fleetCombineStage = null;
	private ExtraStage sceneHelperStage = null;

	/**
	 * 戦闘に関わるシーン一覧
	 */
	private Set<String> battleSceneSet = null;

	/**
	 * 動作fps
	 */
	private IntegerProperty updateFps = new SimpleIntegerProperty(5);
	private Map<Integer, BooleanProperty> fpsMenuMap = new HashMap<Integer, BooleanProperty>(){{
		put(1, updateFps01Flg);
		put(3, updateFps03Flg);
		put(5, updateFps05Flg);
		put(10, updateFps10Flg);
		put(15, updateFps15Flg);
		put(30, updateFps30Flg);
		put(60, updateFps60Flg);
	}};

	/**
	 * 短時間タイマー
	 */
	Timer shortIntervalTimer = null;

	/**
	 * 大破状態だとtrueになる
	 */
	private BooleanProperty marchBlockerStatus = new SimpleBooleanProperty(false);

	/**
	 * 各種戦闘画面の画像を更新するルーチン
	 */
	private BiConsumer<String, BufferedImage> setImage = null;

	/**
	 * 遠征のタイマー情報を更新するルーチン
	 */
	private BiConsumer<Date, Integer> setExpTimer = null;

	/**
	 * 遠征の遠征名を更新するルーチン
	 */
	private BiConsumer<String, Integer> setExpInfo = null;

	/**
	 * 遠征の遠征名を取得するルーチン
	 */
	private Function<Integer, String> getExpInfo = null;

	/**
	 * 遠征タイマーの表示を更新するルーチン
	 */
	private Runnable refreshExpTimerString = null;

	/**
	 * 各種サービス
	 */
    @Autowired
    private ExtraStageFactory factory;
    @Autowired
    private SettingService setting;
    @Autowired
    private UtilityService utility;
    @Autowired
    private ScreenshotService screenshot;
    @Autowired
    private PictureProcessingService pictureProcessing;
    @Autowired
    private SceneRecognitionService sceneRecognition;
    @Autowired
	private CharacterRecognitionService characterRecognition;
	
    /**
     * ログにテキストを追加
     * @param text
     */
    private void addLogText(String text){
        Platform.runLater(() -> {
        	messageLogText.set(String.format(
                    "%s%s %s%n",
                    messageLogText.get(),
                    utility.getDateStringShort(),
                    text
            ));
        });
    }
    
    /**
     * 長い周期で行われるタスクを設定
     */
    private class LongIntervalTask extends TimerTask{
        public void run(){
            // スクリーンショットが撮影可能な場合の処理
            if(screenshot.canGetScreenshot()){
                // ゲーム画面の位置が移動した際の処理
                if(!disableCheckMovedPositionFlg.get() && screenshot.isMovedPosition()){
                    addLogText("【位置ズレ検知】");
                    addLogText("自動で再取得を試みます...");
                    getPositionCommand();
                }
            }else if(setting.<Boolean>getSetting("AutoGetPositionFlg")){
                addLogText("【自動座標認識】");
                addLogText("自動で再取得を試みます...");
                getPositionCommand();
            }
        }
    }
    
    /**
     * 短い周期で行われるタスクを設定
     */
    private class ShortIntervalTask extends TimerTask{
        public void run(){
            // スクリーンショットが撮影可能な場合の処理
            if(screenshot.canGetScreenshot()){
                // 画像を取得
                final BufferedImage frame = screenshot.getScreenshot();

                // シーンを読み取り、結果をメイン画面に表示する
				final String scene = sceneRecognition.judgeScene(frame);
				final String homeType = sceneRecognition.judgeHomeType(frame);
				final String sceneMessage = String.format("シーン判定：%s%s", scene.isEmpty() ? "[不明]": scene, homeType.isEmpty() ? "" : "(" + homeType + ")");
                Platform.runLater(() -> {
                	nowSceneText.set(sceneMessage);
                });

                // 母港に帰投した際
				if(getExpInfo != null && setExpTimer != null && setExpInfo != null && scene.equals("母港")){
					// 支援系の遠征は即座にリセットする
					for(int i = 0; i < TimerModel.EXPEDITION_COUNT; ++i){
						String temp_string = getExpInfo.apply(i);
						if(getExpInfo.apply(i).contains("支援任務")){
							setExpInfo.accept("？", i);
							setExpTimer.accept(new Date(), i);
						}
					}
				}

				// マップ画面もしくは母港画面に遷移した際
				if (scene.equals("母港") || scene.equals("マップ")) {
					// 大破進撃フラグをOFFにする
					marchBlockerStatus.set(false);
				}

                // 戦闘振り返り機能が有効になっていた際、特定シーンの画像を転送する
                if(openBattleSceneReflectionFlg.get()){
					String scene_ = scene.contains("ドロップ") ? "ドロップ" : scene;
                    if(battleSceneSet.contains(scene_)){
                        setImage.accept(scene_, frame);
                    }
                }

                // 各種タイマー機能が有効になっていた際、画像認識により時刻を随時更新する
                if(openTimerFlg.get()){
                    if(scene.equals("遠征個別") || scene.equals("遠征中止")){
                        final long duration = characterRecognition.getExpeditionRemainingTime(frame);
                        if(setExpTimer != null && setExpInfo != null && duration >= 0){
                            final String expeditionId = characterRecognition.getSelectedExpeditionId(frame);
                            final Map<Integer, String> fieetIds = characterRecognition.getExpeditionFleetId(frame);
                            for(Map.Entry<Integer, String> pair : fieetIds.entrySet()){
                                if(pair.getValue().equals(expeditionId)
										//イベント用の支援遠征は、通常海域用の支援遠征と区別できないため
										|| pair.getValue().equals("S1") && expeditionId.equals("33")
										|| pair.getValue().equals("S2") && expeditionId.equals("34")){
                                	Date date = new Date(new Date().getTime() + duration * 1000);
                                	String name = characterRecognition.getExpeditionNameById(pair.getValue());
                                    setExpTimer.accept(date, pair.getKey() - 2);
                                    setExpInfo.accept(name, pair.getKey() - 2);
									setting.setSetting(String.format("ExpTimer%d", pair.getKey() - 2),
											new ArrayList<String>(Arrays.asList(
												utility.DateToISO8601(date),
												name
											))
									);
                                    break;
                                }
                            }
                        }
                    }
                }

                // 大破進撃防止機能が有効になっていた際、MVP画面から大破していないかを判断する
				if (marchBlockerFlg.get()) {
					if (scene.equals("MVP")) {
						boolean hardDamageFlg = sceneRecognition.judgeHardDamage(frame);
						if (hardDamageFlg && !marchBlockerStatus.get()) {
							marchBlockerStatus.set(true);
							addLogText("【警告】");
							addLogText("大破状態の艦がいます。進撃できません");
							utility.showDialog("大破状態の艦がいます。進撃できません", "大破進撃防止", Alert.AlertType.ERROR);
						}
					}
				}
            }
            if(refreshExpTimerString != null){
                refreshExpTimerString.run();
            }
        }
    }
    
    /**
     * コンストラクタ
     */
    public MainModel() {
    	System.out.println("DEBUG MainApp - MainModel#MainModel");

    	// 設定変更時に情報を記録する
    	autoGetPositionFlg.addListener((ob, o, n) -> setting.setSetting("AutoGetPositionFlg", n));
		disableCheckMovedPositionFlg.addListener((ob, o, n) -> setting.setSetting("DisableCheckMovedPositionFlg", n));
    	blindNameTextFlg.addListener((ob, o, n) -> setting.setSetting("BlindNameTextFlg", n));
    	specialGetPosFlg.addListener((ob, o, n) -> setting.setSetting("SpecialGetPosFlg", n));
    	saveWindowPositionFlg.addListener((ob, o, n) -> setting.setSetting("SaveWindowPositionFlg", n));
		marchBlockerFlg.addListener((ob, o, n) -> setting.setSetting("MarchBlockerFlg", n));
		updateFps.addListener((ob, o, n) -> setting.setSetting("UpdateFps", n));
    }
    
    /**
     * 初期化
     * @throws IOException 
     * @throws JsonProcessingException 
     */
    public void initialize() throws JsonProcessingException, IOException{
    	System.out.println("DEBUG MainApp - MainModel#initialize");

    	// 設定を読み込んだ上で画面に反映する
    	autoGetPositionFlg.set(setting.getSetting("AutoGetPositionFlg"));
		disableCheckMovedPositionFlg.set(setting.getSetting("DisableCheckMovedPositionFlg"));
    	blindNameTextFlg.set(setting.getSetting("BlindNameTextFlg"));
    	specialGetPosFlg.set(setting.getSetting("SpecialGetPosFlg"));
    	saveWindowPositionFlg.set(setting.getSetting("SaveWindowPositionFlg"));
		marchBlockerFlg.set(setting.getSetting("MarchBlockerFlg"));
		updateFps.set(setting.getSetting("UpdateFps"));

		// FPS設定によって、メニューのチェック状態を変更
		boolean flg = false;
		for(Map.Entry<Integer, BooleanProperty> pair : fpsMenuMap.entrySet()){
			if (updateFps.get() == pair.getKey()){
				flg = true;
				pair.getValue().set(true);
			}else{
				pair.getValue().set(false);
			}
		}
		if(!flg){
			final int defaultFps = setting.getDefaultSetting("UpdateFps");
			fpsMenuMap.get(defaultFps).set(true);
			updateFps.set(defaultFps);
		}

		// 選択系メニューにおける処理
		updateFps01Flg.addListener((n) -> updateFps.set(1));
		updateFps03Flg.addListener((n) -> updateFps.set(3));
		updateFps05Flg.addListener((n) -> updateFps.set(5));
		updateFps10Flg.addListener((n) -> updateFps.set(10));
		updateFps15Flg.addListener((n) -> updateFps.set(15));
		updateFps30Flg.addListener((n) -> updateFps.set(30));
		updateFps60Flg.addListener((n) -> updateFps.set(60));

    	// Beanの初期化
    	pictureProcessing.initialize();
    	
        // 長周期で実行されるタイマー
        final Timer longIntervalTimer = new Timer();
        longIntervalTimer.schedule(new LongIntervalTask(), 0, 1000);
        
        // 短周期で実行されるタイマー
        shortIntervalTimer = new Timer();
        shortIntervalTimer.schedule(new ShortIntervalTask(), 0, 1000 / updateFps.get());
		updateFps.addListener((ob, o, n) -> {
			shortIntervalTimer.cancel();
			shortIntervalTimer = new Timer();
			Platform.runLater(() -> shortIntervalTimer.schedule(new ShortIntervalTask(), 0, 1000 / n.intValue()));
			addLogText(String.format("動作fps：%d",n.intValue()));
		});
    }
    
	/**
     * 終了コマンド
     */
	public void exitCommand() {
		Platform.exit();
		System.exit(0);
	}
	
	/**
	 * 座標取得コマンド
	 */
	public void getPositionCommand() {
		addLogText("【座標取得】");
		
		// 取得操作を行う
        final boolean getPositionFlg = screenshot.trySearchGamePosition();

        // 取得に成功したか否かで処理を分ける
        if(getPositionFlg){
            // ゲーム座標を取得する
            final Rectangle rect = screenshot.getPosition();

            // 取得したゲーム座標を記録する
            addLogText("座標取得：OK");
            addLogText(String.format("取得位置：(%d,%d)-%dx%d",
                    rect.x, rect.y, rect.width, rect.height));
            
            // スクリーンショットを使用可能にする
            disableSaveScreenshotFlg.set(false);
        }else{
            addLogText("座標取得：NG");
            // スクリーンショットを使用不可にする
            disableSaveScreenshotFlg.set(true);
        }
	}
	
	/**
	 * スクショコマンド
	 */
	public void saveScreenshotCommand() {
		addLogText("【スクリーンショット】");
        if(screenshot.canGetScreenshot()){
            final BufferedImage screenShot = screenshot.getScreenshot();
            final BufferedImage processedImage = pictureProcessing.getProcessedImage(screenShot);
            final String fileName = String.format("%s.png", utility.getDateStringLong());
            try {
                ImageIO.write(processedImage, "png", new File(String.format("pic\\%s", fileName)));
                addLogText(String.format("ファイル名：%s", fileName));
            } catch (IOException e) {
                e.printStackTrace();
                addLogText("エラー：スクリーンショットの保存に失敗しました。");
            }
        }else{
            addLogText("エラー：スクリーンショットを取得できません。");
        }
	}
	
    /**
     * スクショの保存先であるpicフォルダを開く
     * パス指定の時点で明らかなように、Windowsにしか対応していない
     */
    public void openPicFolderCommand(){
        try {
            final Runtime rt = Runtime.getRuntime();
            String cmd = String.format("explorer %s\\pic", System.getProperty("user.dir"));
            rt.exec(cmd);
        } catch (IOException e) {
            utility.showDialog("picフォルダを開けませんでした。", "IOエラー", AlertType.ERROR);
        }
    }

	/**
	 * GCするようJVMに要請する(※気休め)
	 */
	public void runFullGCCommand(){
		Runtime rt = Runtime.getRuntime();
		rt.gc();
	}

	/**
	 * 戦闘振り返り画面を開くコマンド
	 */
	public void openBattleSceneReflectionCommand() {
		// nullでない＝既にそのウィンドウが開いている＝これ以上開く必要はない
		if(battleSceneReflectionStage != null) {
			return;
		}
		
		// 動作ログに残す
		addLogText("【ウィンドウ】");
		addLogText("名称：戦闘振り返り画面");
		
		// ウィンドウのStageを作成する
		try {
			battleSceneReflectionStage = factory.create("/fxml/BattleSceneReflectionView.fxml", "BattleSceneReflectionWindow");
		} catch (IOException e) {
			addLogText("エラー：IOエラーが発生しました。");
			e.printStackTrace();
			return;
		}

		// タイトルを設定する
		battleSceneReflectionStage.setTitle("戦闘振り返り画面");
		
		// 既にウィンドウを表示した、というフラグを立てる
		openBattleSceneReflectionFlg.set(true);

		// ウィンドウが閉じられた際の処理を記述する
		// (再度ウィンドウを開けるようにリセット)
		battleSceneReflectionStage.setOnCloseRequest(() -> {
			battleSceneReflectionStage = null;
			openBattleSceneReflectionFlg.set(false);
		});

		// Controllerから値・メソッドを受け取る
		BattleSceneReflectionController controller = battleSceneReflectionStage.getController();
		battleSceneSet = BattleSceneReflectionModel.SceneList;
		setImage = (key, image) -> controller.setImage(key, image);

		// ウィンドウを表示する
		battleSceneReflectionStage.show();
	}
	
	/**
	 * 各種タイマー画面を開くコマンド
	 */
	public void openTimerCommand() {
		// nullでない＝既にそのウィンドウが開いている＝これ以上開く必要はない
		if(timerStage != null) {
			return;
		}
		
		// 動作ログに残す
		addLogText("【ウィンドウ】");
		addLogText("名称：各種タイマー画面");
		
		// ウィンドウのStageを作成する
		try {
			timerStage = factory.create("/fxml/TimerView.fxml", "TimerWindow");
		} catch (IOException e) {
			addLogText("エラー：IOエラーが発生しました。");
			e.printStackTrace();
		}
		
		// タイトルを設定する
		timerStage.setTitle("各種タイマー画面");
		
		// 既にウィンドウを表示した、というフラグを立てる
		openTimerFlg.set(true);
		
		// ウィンドウが閉じられた際の処理を記述する
		// (再度ウィンドウを開けるようにリセット)
		timerStage.setOnCloseRequest(() -> {
			timerStage = null;
			openTimerFlg.set(false);
		});

		// Controllerから値・メソッドを受け取る
		TimerController controller = timerStage.getController();
		setExpTimer = (date, index) -> controller.setExpTimer(date, index);
		setExpInfo = (info, index) -> controller.setExpInfo(info, index);
		getExpInfo = (index) -> controller.getExpInfo(index);
		refreshExpTimerString = (() -> controller.refreshExpTimerString());

		// 事前に値をセットする
		for(int i = 0; i < TimerModel.EXPEDITION_COUNT; ++i){
			List<String> timer = setting.getSetting(String.format("ExpTimer%d",i));
			setExpInfo.accept(timer.get(1), i);
			setExpTimer.accept(utility.ISO8601ToDate(timer.get(0)), i);
		}

		// ウィンドウを表示する
		timerStage.show();
	}

	/**
	 * 編成まとめ画面を開くコマンド
	 */
	public void openFleetCombineCommand(){
		// nullでない＝既にそのウィンドウが開いている＝これ以上開く必要はない
		if(fleetCombineStage != null) {
			return;
		}

		// 動作ログに残す
		addLogText("【ウィンドウ】");
		addLogText("名称：編成まとめ画面");

		// ウィンドウのStageを作成する
		try {
			fleetCombineStage = factory.create("/fxml/FleetCombineView.fxml", "FleetCombineWindow");
		} catch (IOException e) {
			addLogText("エラー：IOエラーが発生しました。");
			e.printStackTrace();
		}

		// タイトルを設定する
		fleetCombineStage.setTitle("編成まとめ画面");

		// 既にウィンドウを表示した、というフラグを立てる
		openFleetCombineFlg.set(true);

		// ウィンドウが閉じられた際の処理を記述する
		// (再度ウィンドウを開けるようにリセット)
		fleetCombineStage.setOnCloseRequest(() -> {
			fleetCombineStage = null;
			openFleetCombineFlg.set(false);
		});

		// ウィンドウを表示する
		fleetCombineStage.show();
	}

	/**
	 * 画像認識支援画面を開くコマンド
	 */
	public void openSceneHelperCommand() {
		// nullでない＝既にそのウィンドウが開いている＝これ以上開く必要はない
		if(sceneHelperStage != null) {
			return;
		}

		// 動作ログに残す
		addLogText("【ウィンドウ】");
		addLogText("名称：画像認識支援画面");
		
		// ウィンドウのStageを作成する
		try {
			sceneHelperStage = factory.create("/fxml/SceneHelperView.fxml", "SceneHelperWindow");
		} catch (IOException e) {
			addLogText("エラー：IOエラーが発生しました。");
			e.printStackTrace();
		}
		
		// タイトルを設定する
		sceneHelperStage.setTitle("画像認識支援画面");
		
		// 既にウィンドウを表示した、というフラグを立てる
		openSceneHelperFlg.set(true);
		
		// ウィンドウが閉じられた際の処理を記述する
		// (再度ウィンドウを開けるようにリセット)
		sceneHelperStage.setOnCloseRequest(() -> {
			sceneHelperStage = null;
			openSceneHelperFlg.set(false);
		});

		// ファイルドロップ時の処理を設定する
		sceneHelperStage.setOnDragOver();
		sceneHelperStage.setOnDragDropped((File file) -> {
			try {
				BufferedImage image = ImageIO.read(file);
				sceneHelperStage.<SceneHelperController>getController().setImage(image);
			}catch (IOException e){
				e.printStackTrace();
			}
		});

		// ウィンドウを表示する
		sceneHelperStage.show();
	}
	
    /**
     * ソフトウェアの更新が来ているかをチェックする
     */
    public void checkVersionCommand(){
        try {
            addLogText("【更新チェック】");
            // 更新情報を表すテキストファイルをダウンロードする
            final String checkText = utility.downloadTextData("https://raw.githubusercontent.com/YSRKEN/KAMO/master/version.txt");
            if(checkText.isEmpty())
                throw new IOException();
            // 更新文字列は「1,1.0.0」のような書式になっているはずなので確認する
            final String[] temp = checkText.split(",");
            if(temp.length < 2){
                throw new NumberFormatException();
            }
            // 情報を読み取っていく
            final int revision = Integer.parseInt(temp[0]);
            addLogText(String.format("現在のバージョン：%s, リビジョン：%d",
                    Constant.SOFTWARE_VER, Constant.SOFTWARE_REVISION));
            addLogText(String.format("最新のバージョン：%s, リビジョン：%d",
                    temp[1], revision));
            if(Constant.SOFTWARE_REVISION < revision){
                String message = String.format(
                        "より新しいバージョンが見つかりました。%n現在のバージョン：%s%n最新のバージョン：%s%nダウンロードサイトを開きますか？",
                        Constant.SOFTWARE_VER, temp[1]
                );
                final boolean openUrlFlg = utility.showChoiceDialog(message, "更新チェック");
                if(openUrlFlg){
                    final Desktop desktop = Desktop.getDesktop();
                    try{
                        desktop.browse(new URI(Constant.SOFTWARE_URL));
                    }catch( Exception e ){
                        e.printStackTrace();
                    }
                }
            }else{
                addLogText("このソフトウェアは最新です。");
            }
        }catch(NumberFormatException | IOException e){
            e.printStackTrace();
            addLogText("エラー：更新データを確認できませんでした。");
        }
    }
	
    /**
     * オンラインヘルプ(Wiki)を開く
     */
    public void openWikiCommand(){
        final Desktop desktop = Desktop.getDesktop();
        try{
            desktop.browse(new URI(Constant.HELP_URL));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * バージョン情報を表示する
     */
    public void aboutCommand(){
        final String contentText = String.format("ソフト名：%s%nバージョン：%s%n作者：%s",
        		Constant.SOFTWARE_NAME,
        		Constant.SOFTWARE_VER,
        		Constant.SOFTWARE_AUTHOR
        );
        utility.showDialog(contentText, "バージョン情報");
    }
}
