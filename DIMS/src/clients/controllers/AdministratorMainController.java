package clients.controllers;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;

import com.orsoncharts.util.json.JSONArray;
import com.orsoncharts.util.json.JSONObject;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import clients.SceneManager;
import clients.customcontrols.CalendarObject;
import clients.customcontrols.CustomDialog;
import clients.customcontrols.ScheduleObject;
import tools.NetworkProtocols;
import tools.Statics;
import tools.Toolbox;

public class AdministratorMainController implements Initializable {

	boolean isDraging = false;
	
	AdministratorMainController me;
	SceneManager sManager;
	
	private ObjectInputStream fromServer;
	private ObjectOutputStream toServer;
	private ArrayList<String> rList;
	private JSONArray jarray;
	private String uID, uName;
	
	/* ȭ�� �ֻ�� */
	@FXML StackPane stack;
	@FXML Label idField;
	@FXML Label dateText, dateTime;
	
	/* ���� ���� �޴� */
	@FXML BorderPane Schedule_Main;
	@FXML AnchorPane schedule_board;
	@FXML ComboBox<String> schedule_view_mode;
	private ArrayList<ScheduleObject> sObjList = new ArrayList<ScheduleObject>();
	private String SCHEDULE_DISPLAY_MODE = "�޷¸��";
	private JSONArray SCHEDULE_DISPLAY_DATA;
	private String startDateString="", endDateString = "";
		/* ��ƺ��� �޴� */
		@FXML DatePicker startDate, endDate;
		@FXML CheckBox search_daily, search_weekly, search_monthly;
		
		/* ������ü �����޴� */
		@FXML AnchorPane modifyPanel;
		private Label scheduleID;
		@FXML TextField modiTitle;
		@FXML DatePicker modiDate;
		@FXML ComboBox<String> hourPicker;
		@FXML ComboBox<String> minitePicker;		
		@FXML CheckBox cate_important, cate_event, cate_normal;
		@FXML TextArea contentArea;
		
		
	/* �̵�� �÷��̾� �޴� */
	@FXML BorderPane MEDIA;							// �̵�� ��
	@FXML MediaView MEDIA_VIEW;						// �̵�� ��� ����
	@FXML Slider TIME_SLIDER;						// ������ Ÿ�� �����̴�
	@FXML Label TIME_TEXT;							// ������ �ð� ��� - ���� / ��
	@FXML Label MEDIA_TITLE;						// ������ ���� ���
	private MediaPlayer player;
	// playTest()	: ������ �ø��� �׽�Ʈ ��ư
	// onPlay()		: ����ϱ�
	// onPause()	: �Ͻ������ϱ�
	// onStop()		: �����ϱ�
	
	
	/* �Խ��� �޴� */
	@FXML BorderPane BOARD;							// �Խ��� ��
	@FXML VBox BOARD_LIST_VIEW;						// �Խ��� - �Խñ� ����Ʈ Ȯ�� ��
	@FXML ListView<HBox> BOARD_LIST;				// �Խ��� - ����Ʈ
	@FXML VBox BOARD_CONTENT_VIEW;					// �Խ��� - �Խñ� ���� Ȯ�� ��
		@FXML Label BOARD_CONTENT_TITILE;			// �Խ��� - �Խñ� ���� ����
		@FXML Label BOARD_CONTENT_CATEGORY;			// �Խ��� - �Խñ� ī�װ���
		@FXML TextArea BOARD_CONTENT_AREA;			// �Խ��� - �Խñ� ����
		@FXML Label BOARD_CONTENT_CREATOR;			// �Խ��� - �Խñ� �ۼ���
		@FXML Label BOARD_CONTENT_CREATE_AT;		// �Խ��� - �Խñ� �ۼ�����
	private ObservableList<HBox> boardListData;		// �Խ��� - ����Ʈ ������
	@FXML VBox BOARD_WRITE;							// �Խ��� - �Խñ� �ۼ�
	@FXML ComboBox<String> BOARD_CATEGORY_SELECTOR;	// �Խ��� - �˻� �� ī�װ��� ������ �޺��ڽ�
	@FXML TextField BOARD_TITLE_FIELD;				// �Խ��� - �˻� Ű���带 �Է��ϴ°�
	@FXML TextField BOARD_TITLE;					// �Խ��� - �ۼ��� ����
	@FXML TextArea	BOARD_CONTENT;					// �Խ��� - �ۼ��� ����
	@FXML ComboBox<String> BOARD_W_CSELECTOR;		// �Խ��� - �ۼ� �� ī�װ��� ������ �޺��ڽ�
	// onNotice()	 : �������� ��ư Ŭ��
	// onRequest()   : ���ǻ��� ��ư Ŭ��
	// onFree()		 : �����Խ��� ��ư Ŭ��
	// onBoardSearch : �Խ��� �� �˻� ��ư Ŭ��
	
	 /* �޼��� �޴� */
	 @FXML TabPane MESSAGE;                     // �޼��� ��
	 @FXML BorderPane USERINFO;              // �Ż�������ȸ ����
	 @FXML TabPane STUDNETMANAGER;           // �л� ����� ���� ����
	 @FXML TextField searchField;               // �޼��� ������ �˻� �ʵ�
	 @FXML ListView<HBox> recieverList;            // �޼��� ������ ���
	 @FXML ListView<HBox> messageList;         // �޼��� ���� ���
	 @FXML ListView<HBox> StudentList;         // �л� ��ȸ ���
	 @FXML ListView<HBox> sendmessageList;
	 private ObservableList<HBox> recieverListData;   // ������ ��� ������
	 private ObservableList<HBox> recievermessageListData; // �޽��� ���� ������
	 private ObservableList<HBox> StudentListData; // �л� ��ȸ ��� ������
	 private ObservableList<HBox> StudentManagerListData; // ����� ��ȸ ��� ������
	 private ObservableList<HBox> sendmessageListData;
	 @FXML TextArea display_reciever;            // ���õ� �޼��� ������ ��� (�й�, �̸�)
	 @FXML TextField msgTitle;                  // �޼��� ����
	 @FXML TextArea msgContent;                  // �޼��� ����
	 private int saved = -1;
	 // onAdd()       : recieverList���� ������ �����ڸ� display_reciever�� �߰� 
	 // onSendMsg()  : �޼��� ����
	 // onSearch()   : searchField�� ���� ��ȭ�� ���� recieverList�� ������ �ٲ�

	 /* �Ż����� ��ȸ �޴� */
	 @FXML TextField studenttextField;           // �л� �Ż�������ȸ �˻� �ʵ�
	 
	 /* �ܹ� ���� �޴� */
	 private ObservableList<HBox> StudentMenagerListCheckData; // ����� �ο� ����Ʈ ������
	 @FXML TextField WeabakField;				//�ܹ��л���ȸ �ؽ�Ʈ�ʵ�
	 @FXML ListView<HBox> StudentWeabakList;     // �л� �ܹ� ���� ��ȸ ���
	 private ObservableList<HBox> StudentWeabakListData; //�ܹ� ��� ������
	 
	 /* ����� ���� �޴� */
	 @FXML TextField PMField;                    //�������ȸ �ؽ�Ʈ�ʵ�
	 @FXML ListView<HBox> StudentMenagerListCheck; // �л� ����� ���� ����Ʈ;
	 @FXML ListView<HBox> StudentManagerList;    // �л� ����� ���� ��ȸ ���
	 
	 
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		/* ���������� �ʱ�ȭ */
		
		startDate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				startDateString = startDate.getValue().toString();
			}
		});
		
		endDate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				endDateString = endDate.getValue().toString();
			}
		});
		
		cate_important.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				cate_event.setSelected(false);
				cate_normal.setSelected(false);
			}
		});
		
		cate_event.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				cate_important.setSelected(false);
				cate_normal.setSelected(false);
			}
		});
		
		cate_normal.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				cate_important.setSelected(false);
				cate_event.setSelected(false);
			}
		});
		
		search_daily.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				search_weekly.setSelected(false);
				search_monthly.setSelected(false);
			}
		});
		search_weekly.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				search_daily.setSelected(false);
				search_monthly.setSelected(false);
			}
		});
		search_monthly.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				search_daily.setSelected(false);
				search_weekly.setSelected(false);
			}
		});
		
		scheduleID = new Label();
		scheduleID.setVisible(false);
		modifyPanel.getChildren().add(scheduleID);
		schedule_view_mode.getItems().addAll("�׸�����", "����Ʈ���", "�޷¸��");
		
		hourPicker.getItems().addAll("00","01","02","03","04","05","06","07","08","09","10"
									,"11","12","13","14","15","16","17","18","19","20","21"
									,"22","23");
		
		minitePicker.getItems().addAll("00","01","02","03","04","05","06","07","08","09"
									,"10","11","12","13","14","15","16","17","18","19"
									,"20","21","22","23","24","25","26","27","28","29"
									,"30","31","32","33","34","35","36","37","38","39"
									,"40","41","42","43","44","45","46","47","48","49"
									,"50","51","52","53","54","55","56","57","58","59");
		
		modifyPanel.setDisable(true);
		/* �ð� �ʱ�ȭ */
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				Calendar cal = Calendar.getInstance();
				java.util.Date date = cal.getTime();
				dateText.setText(Toolbox.getCurrentTimeFormat(date, "YYYY-MM-dd (E)"));
				dateTime.setText(Toolbox.getCurrentTimeFormat(date, "a hh:mm:ss"));
			}
		}));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
		
		/* �޼����� �ʱ�ȭ */
		display_reciever.setEditable(false);
		recieverListData = FXCollections.observableArrayList();
		recievermessageListData = FXCollections.observableArrayList();
		boardListData = FXCollections.observableArrayList();
		StudentListData = FXCollections.observableArrayList();
	    StudentManagerListData = FXCollections.observableArrayList();
	    sendmessageListData = FXCollections.observableArrayList();

	    StudentWeabakListData = FXCollections.observableArrayList();
	    StudentMenagerListCheckData = FXCollections.observableArrayList();
	    
		
		/* �Խ����� �ʱ�ȭ */
		BOARD_CATEGORY_SELECTOR.getItems().addAll("��ü","��������", "���ǻ���", "�����Խ���");
		BOARD_W_CSELECTOR.getItems().addAll("��������", "���ǻ���", "�����Խ���");
		BOARD_WRITE.setVisible(false);
		BOARD_LIST_VIEW.setVisible(true);
		BOARD_CONTENT_VIEW.setVisible(false);
		
		shutdown();
		
		
		
	}
	
	public void INIT_CONTROLLER(SceneManager manager, ObjectInputStream fromServer, ObjectOutputStream toServer)
	{
		this.sManager = manager;
		this.fromServer = fromServer;
		this.toServer = toServer;
	}

	public void shutdown()
	{
		BOARD.setVisible(false);
		MESSAGE.setVisible(false);
		MEDIA.setVisible(false);
		STUDNETMANAGER.setVisible(false);
		USERINFO.setVisible(false);
		Schedule_Main.setVisible(false);
	}
	
	public void startListener()
	{
		new Listener().start();
	}
	
	class Listener extends Thread
	{
		@SuppressWarnings("unchecked")
		@Override
		public void run()
		{
			synchronized (this)
			{
				try
				{
					// �̰� �ص� �ǰ� ���ص��Ǵµ�.. ���� �α��� �ױ����� �갡 �������۵ɶ��� �־ 1�� ��ٸ�����
					System.out.println("LoginController ������ ����ɶ����� ���");
					System.out.println("���⿡ �ε��� �����غ��");
					this.wait(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			System.out.println("AdministratorMainController ������ ����");
			JSONObject o = Toolbox.createJSONProtocol(NetworkProtocols.BOARD_LIST_REQUEST);
			o.put("category", "��������");
			
			try
			{
				while(true)
				{
					try
					{
						JSONObject line = (JSONObject)fromServer.readObject();
						
						String type = line.get("type").toString();
						
						if(type.equals(NetworkProtocols.EXIT_RESPOND))
						{
							break;
						}
						else if(type.equals(NetworkProtocols.RECIEVE_READY))
						{
							JSONObject protocol = new JSONObject();
							protocol.put("type", NetworkProtocols.RECIEVE_READY_OK);
							sendProtocol(protocol);
						}
						else if(type.equals(NetworkProtocols.SHOW_MESSAGE_DIALOG))
						{
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									CustomDialog.showMessageDialog(line.get("msg").toString(), sManager.getStage());
								}
							});
						}
						else if(type.equals(NetworkProtocols.PLZ_REQUEST))
						{
							JSONObject protocol = new JSONObject();
							protocol.put("type", NetworkProtocols.WINDOW_INIT_PROPERTY);
							protocol.put("category", "��������");
							sendProtocol(protocol);							
						}
						else if(type.equals(NetworkProtocols.WINDOW_INIT_PROPERTY))
						{
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									uName = line.get("uName").toString();
									uID = line.get("uID").toString();
									idField.setText(uName+"��");
								}
							});
						}
						else if(type.equals(NetworkProtocols.MESSAGE_USER_LIST_RESPOND))
						{
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									createReciverList((ArrayList<String>)line.get("rcList"));
								}
							});
						}
						else if(type.equals(NetworkProtocols.ENROLL_BOARD_RESPOND))
						{
							System.out.println("�Խñ� ��� ����");
							BOARD_WRITE.setVisible(false);
							BOARD_LIST_VIEW.setVisible(true);

							toServer.writeObject(o);
							toServer.flush();
						}
						else if(type.equals(NetworkProtocols.BOARD_LIST_RESPOND))
						{
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									createBoardList((JSONArray)line.get("board_list"));
								}
							});
						}
						else if(type.equals(NetworkProtocols.MESSAGE_SEND_RESPOND))
		                {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									CustomDialog.showMessageDialog("�޼��� �߼� ����!", sManager.getStage());
								}
							});
						}
						else if(type.equals(NetworkProtocols.MESSAGE_RECIEVE_LIST_RESPOND))
						{
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createRecivedmessage((JSONArray)line.get("message_list"));
								}
							});
						}
						else if(type.equals(NetworkProtocols.MESSAGE_SEND_LIST_RESPOND))
						{
								Platform.runLater(new Runnable() {
								@Override
								public void run() {
									createSendMassageList((JSONArray)line.get("message_list"));
								}
							});
						}
						else if(type.equals(NetworkProtocols.BOARD_CONTENT_RESPOND))
						{
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									BOARD_CONTENT_AREA.setEditable(true);
									BOARD_CONTENT_TITILE.setText(line.get("�Խñ�����").toString());
									BOARD_CONTENT_CATEGORY.setText(line.get("ī�װ���").toString());
									BOARD_CONTENT_CREATOR.setText(line.get("�̸�").toString());
									BOARD_CONTENT_AREA.setText(line.get("�Խñۺ���").toString());
									BOARD_CONTENT_CREATE_AT.setText(line.get("�ۼ�����").toString());
									BOARD_CONTENT_AREA.setEditable(false);
									BOARD_LIST_VIEW.setVisible(false);
									BOARD_WRITE.setVisible(false);
									BOARD_CONTENT_VIEW.setVisible(true);									
								}
							});
						}
						else if(type.equals(NetworkProtocols.BOARD_SEARCH_RESPOND))
						{
							JSONArray arr = (JSONArray)line.get("boardlist");
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									createBoardList(arr);									
								}
							});

						}
						else if(type.equals(NetworkProtocols.BOARD_NO_SEARCH_RESULT))
						{
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									CustomDialog.showMessageDialog("�Խù��� �������� �ʽ��ϴ�.", sManager.getStage());
								}
							});
						}
						else if(type.equals(NetworkProtocols.VIDIO_RESPOND))
						{
							byte[] videoData = (byte[])line.get("vdata");
							Files.write(Paths.get("./tmp.mp4"), videoData);
							
							File file = new File("./tmp.mp4");
							
							MEDIA_TITLE.setText(file.getName());
							
							Media me = new Media(file.toURI().toString());
							player = new MediaPlayer(me);

							player.currentTimeProperty().addListener(new InvalidationListener() {
								
								@Override
								public void invalidated(Observable observable) {
									TIME_TEXT.setText(Toolbox.formatTime(player.getCurrentTime(), player.getTotalDuration()));
								}
							});	
							
							TIME_SLIDER.valueProperty().addListener(new InvalidationListener() {
								
								@Override
								public void invalidated(Observable observable) {
									player.seek(player.getTotalDuration().multiply(TIME_SLIDER.getValue() / 100.0));
									
								}
							});
							
							double w=1024,h=768;
							
							MEDIA_VIEW.setFitWidth(w);
							MEDIA_VIEW.setFitHeight(h);
							MEDIA_VIEW.setMediaPlayer(player);
						}
						else if(type.equals(NetworkProtocols.MESSAGE_CONTENT_RESPOND))
						{
							System.out.println("����");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									CustomDialog cd = new CustomDialog(Statics.CHECK_MESSAGE_FXML, Statics.CHECK_MESSAGE_TITLE, sManager.getStage());
									CheckMessageDialog_Controller con = (CheckMessageDialog_Controller) cd.getController();
									con.setWindow(cd);
									con.setProperty(line);
									
									cd.showAndWait();
									
									String v = (String)cd.getUserData();
									System.out.println(v);									
								}
							});

						}
						else if(type.equals(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_RESPOND))
						{
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									SCHEDULE_DISPLAY_DATA = (JSONArray)line.get("todays");
									System.out.println(SCHEDULE_DISPLAY_DATA.toJSONString());
									if(SCHEDULE_DISPLAY_MODE.equals("�׸�����"))
									{
										createScheduleBoard_GRID(SCHEDULE_DISPLAY_DATA);										
									}
									else if(SCHEDULE_DISPLAY_MODE.equals("����Ʈ���"))
									{
										createScheduleBoard_LIST(SCHEDULE_DISPLAY_DATA);
									}
									else if(SCHEDULE_DISPLAY_MODE.equals("�޷¸��"))
									{
										createScheduleBoard_CELL(SCHEDULE_DISPLAY_DATA);
									}
									shutdown();
									Schedule_Main.setVisible(true);
								}
							});
						}
						else if(type.equals(NetworkProtocols.BOARD_MAIN_RESPOND))
						{
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createBoardList((JSONArray)line.get("board_list"));
									shutdown();
									BOARD.setVisible(true);
								}
							});
						}
						else if(type.equals(NetworkProtocols.MESSAGE_SHOW_MESSAGE_TAP_RESPOND))
						{
							Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createRecivedmessage((JSONArray)line.get("message_list"));
								shutdown();
								MESSAGE.setVisible(true);
							}
							});
						}
						else if(type.equals(NetworkProtocols.MONTHLY_SCHEDULE_VIEW_RESPOND))
						{
							SCHEDULE_DISPLAY_DATA = (JSONArray)line.get("todays");
							createScheduleBoard_CELL(SCHEDULE_DISPLAY_DATA, line.get("viewableDate").toString());
						}
						//�Ż����� ��ȸ  ����
						else if(type.equals(NetworkProtocols.SHOW_USER_INFO_TAP_RESPOND))
						{
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									jarray = (JSONArray)line.get("user_list");
									createStudentSearch(jarray);
									shutdown();
									USERINFO.setVisible(true);
								}
							});
						}
						else if(type.equals(NetworkProtocols.USER_CONTENT_RESPOND))
						{
							System.out.println("����");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									CustomDialog cd = new CustomDialog(Statics.CHECK_STUDENT_FXML, Statics.STUDENT_TITLE, sManager.getStage());
									CheckStudentDialog_Controller con = (CheckStudentDialog_Controller) cd.getController();
									System.out.println(con);
									con.setWindow(cd);
									con.setProperty(line);
									
									cd.showAndWait();
									
									String v = (String)cd.getUserData();
									System.out.println(v);									
								}
							});
						}
						else if(type.equals(NetworkProtocols.WEABAK_INFO_TAP_RESPOND))
						{
							Platform.runLater(new Runnable() {
								public void run() {
									jarray = (JSONArray)line.get("weabak_list");
									createWeabakManager(jarray);
									shutdown();
									STUDNETMANAGER.setVisible(true);
									
								}
							});
						}
						else if(type.equals(NetworkProtocols.PLUS_MINUS_TAP_RESPOND))
						{
							Platform.runLater(new Runnable() {
								public void run() {
									jarray = (JSONArray)line.get("plusminus_list");
									createPlusMinusSearch(jarray);
								}
							});
						}
						else if(type.equals(NetworkProtocols.WEQBAK_CONTENT_RESPOND))
						{
									Platform.runLater(new Runnable() {
										public void run() {
											System.out.println(" //�ܹ� ����Ʈ ����");
											JSONObject json = (JSONObject)line.get("weabak_content_list");
											System.out.println(json.get("�̸�"));
											CustomDialog cd = new CustomDialog(Statics.CHECK_WEABAK_FXML,Statics.WEABAK_TITLE, sManager.getStage());
											
											CheckWeabakDialog_Controller cwdc = (CheckWeabakDialog_Controller)cd.getController();
											cwdc.setWindow(cd);
											cwdc.setProperty(json);
											
											cd.showAndWait();
											
											JSONObject action = (JSONObject)cd.getUserData();
											
											if(action.get("action").toString().equals("not"))
											{
												return;
											}
											else
											{
												action.put("type", NetworkProtocols.WEABAK_PROCESS_REQUEST);
												action.put("reqNo", json.get("�ܹڹ�ȣ"));
												System.out.println(action.toJSONString());
												sendProtocol(action);
											}
										}
									});
						}
						else if(type.equals(NetworkProtocols.PLUS_MINUS_TAP_INFO_RESPOND))
						{
							Platform.runLater(new Runnable() {
								public void run() {
									StudentManagerList((JSONArray)line.get("plus_minus_check_list"));
								}
							});
						}
						else if(type.equals(NetworkProtocols.WEABAK_PROCESS_RESPOND))
						{
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									CustomDialog.showMessageDialog("ó���Ǿ����ϴ�.", sManager.getStage());								}
							});
							JSONObject obj = Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_INFO_TAP_REQUEST);
							obj.put("category", "�����");
							sendProtocol(obj);
						}
					}
					catch(ClassNotFoundException e)
					{
						System.out.println(e);
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			System.out.println("AdministratorMainController ������ ����");
		}
	}
	
	public void createBoardList(JSONArray data)
	{
		boardListData.removeAll(boardListData);
		
		HBox box = new HBox();
		Label l1,l2,l3,l4;
		
		l1 = new Label("��ȣ");
		l2 = new Label("�ۼ���");
		l3 = new Label("�� ��");
		l4 = new Label("�ۼ�����");
		
		l1.setMaxWidth(400);
		l2.setMaxWidth(400);
		l3.setMaxWidth(1200);
		l4.setMaxWidth(400);
		
		l1.setTextFill(Color.web("#ffffff"));
		l2.setTextFill(Color.web("#ffffff"));
		l3.setTextFill(Color.web("#ffffff"));
		l4.setTextFill(Color.web("#ffffff"));
		
		l1.setStyle("-fx-background-color : gray; -fx-border-color : black;");
		l2.setStyle("-fx-background-color : gray; -fx-border-color : black;");
		l3.setStyle("-fx-background-color : gray; -fx-border-color : black;");
		l4.setStyle("-fx-background-color : gray; -fx-border-color : black;");
		
		l1.setAlignment(Pos.CENTER);
		l2.setAlignment(Pos.CENTER);
		l3.setAlignment(Pos.CENTER);
		l4.setAlignment(Pos.CENTER);
		
		HBox.setHgrow(l3, Priority.ALWAYS);
		HBox.setHgrow(l2, Priority.ALWAYS);
		HBox.setHgrow(l4, Priority.ALWAYS);
		
		box.getChildren().addAll(l1,l2,l3,l4);
		
		boardListData.add(box);
		
		JSONArray arr = data;
		
		for(Object o : arr)
		{
			JSONObject target = (JSONObject)o;
			int no = Integer.parseInt(target.get("No").toString());
			String name = target.get("�̸�").toString();
			String title = target.get("�Խñ�����").toString();
			Date create_at = (Date) target.get("�ۼ�����");
			
			HBox item = new HBox();
			Label numLabel = new Label("   "+no+"  ");
			numLabel.setStyle("-fx-border-color : black");
			numLabel.setAlignment(Pos.CENTER);
			numLabel.setMaxWidth(400);
			Label nLabel = new Label(name);
			nLabel.setStyle("-fx-border-color : black");
			nLabel.setMaxWidth(400);
			nLabel.setAlignment(Pos.CENTER);
			Label tLabel = new Label(title);
			tLabel.setMaxWidth(1200);
			tLabel.setStyle("-fx-border-color : black");
			tLabel.setMaxWidth(Double.MAX_VALUE);
			tLabel.setAlignment(Pos.CENTER);
			Label cLabel = new Label(create_at.toString());
			cLabel.setMaxWidth(400);
			cLabel.setStyle("-fx-border-color : black");
			cLabel.setAlignment(Pos.CENTER);
			HBox.setHgrow(tLabel, Priority.ALWAYS);
			HBox.setHgrow(nLabel, Priority.ALWAYS);
			HBox.setHgrow(cLabel, Priority.ALWAYS);
			item.getChildren().addAll(numLabel, nLabel, tLabel, cLabel);
			item.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@SuppressWarnings("unchecked")
				@Override
				public void handle(MouseEvent event) {
					if(event.getClickCount()==2)
					{
						JSONObject req = Toolbox.createJSONProtocol(NetworkProtocols.BOARD_CONTENT_REQUEST);
						req.put("No", no);
						try
						{
							toServer.writeObject(req);
							toServer.flush();
						}
						catch(IOException e)
						{
							e.printStackTrace();
						}
					}
					
				}
			});
			
			boardListData.add(item);
		}
		
		BOARD_LIST.setItems(boardListData);
		BOARD_LIST.setVisible(true);
	}
	

	public void createReciverList(ArrayList<String> data)
	{
		recieverListData.removeAll(recieverListData);
		for(String t : data)
		{
			String[] info = t.split(",");
			Label left = new Label(info[0]);
			left.setAlignment(Pos.CENTER);
			left.setStyle("-fx-border-color : black");
			left.setMaxWidth(Double.MAX_VALUE);
			left.setMaxHeight(Double.MAX_VALUE);
			Label right = new Label(info[1]);
			right.setAlignment(Pos.CENTER);
			right.setMaxWidth(Double.MAX_VALUE);
			right.setMaxHeight(Double.MAX_VALUE);
			right.setStyle("-fx-border-color : black");
			HBox item = new HBox();
			//item.setMaxSize(Double.MAX_VALUE, maxHeight);
			item.getChildren().addAll(left, right);
			HBox.setHgrow(right, Priority.ALWAYS);
			HBox.setHgrow(left, Priority.ALWAYS);
			item.setAlignment(Pos.CENTER);
			recieverListData.add(item);
		}
		
		recieverList.setItems(recieverListData);
	}
	
	public void createRecivermessage(ArrayList<String> data)
	{
		System.out.println("���� : "+data.toString());
	      recievermessageListData.removeAll(recievermessageListData);
	      for(String t : data)
	      {
	    	 System.out.println(t);
	         String[] info = t.split(",");
	         CheckBox left = new CheckBox();
	         left.setAlignment(Pos.CENTER_LEFT);
	         left.setMaxHeight(Double.MIN_VALUE);
	         left.setMaxWidth(Double.MIN_VALUE);
	         Label Center = new Label(info[0]);
	         Center.setAlignment(Pos.CENTER_LEFT);
	         Center.setMaxHeight(30);
	         Center.setMaxWidth(150);
	         Center.setStyle("-fx-border-color : black");
	         Label right = new Label(info[1]);
	         right.setAlignment(Pos.CENTER);;
	         right.setMaxHeight(Double.MAX_VALUE);
	         right.setMaxWidth(Double.MAX_VALUE);
	         HBox item = new HBox();
	         item.getChildren().addAll(left, Center,right);
	         HBox.setHgrow(right, Priority.ALWAYS);
	         HBox.setHgrow(Center, Priority.ALWAYS);
	         HBox.setHgrow(left, Priority.ALWAYS);
	         item.setAlignment(Pos.CENTER);
	         recievermessageListData.add(item);
	      }
	      messageList.setItems(recievermessageListData);
	      messageList.setVisible(true);
	   }

	
	public void sendProtocol(JSONObject protocol)
	{
		try
		{
			toServer.writeObject(protocol);
			toServer.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
	
	@FXML private void onMenu_1()
	{
		/* 1�� �޴� Ŭ���� */
		sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.SHOW_USER_INFO_TAP_REQUEST));
	}
	
	@FXML private void onMenu_2()
	{
		/* 2�� �޴� Ŭ���� */
		sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_SHOW_MESSAGE_TAP_REQUEST));
	}
	
	@FXML private void onMenu_3()
	{
		/* 3�� �޴� Ŭ���� */
		sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.BOARD_MAIN_REQUEST));
	}
	
	@FXML private void onMenu_4()
	{
		/* 4�� �޴� Ŭ���� */
		shutdown();
		STUDNETMANAGER.setVisible(true);
	}
	
	@FXML private void onMenu_5()
	{
		/* 5�� �޴� Ŭ���� */
		shutdown();
		MEDIA.setVisible(true);
	}
	
	@SuppressWarnings("unchecked")
	@FXML private void onMenu_6()
	{
		JSONObject req = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_REQUEST);
		req.put("viewtype", SCHEDULE_DISPLAY_MODE);
		sendProtocol(req);
	}
	
	@FXML private void playTest()
	{

	}
	
	@FXML private void onPlay()
	{
		player.play();
	}
	
	@FXML private void onPause()
	{
		player.pause();
	}
	
	@FXML private void onStop()
	{
		TIME_SLIDER.setValue(0.0);
		player.stop();
	}
	
	@FXML private void onLogout()
	{
		sManager.doFullscreen(false);
		sManager.changeListenController("ADMIN_MAIN");
		sManager.changeScene(Statics.LOGIN_WINDOW_FXML);
	}

	@FXML private void onSearch()
	{
		String searchKey = searchField.getText();
		ArrayList<String> newList = new ArrayList<String>();

		for(String s : rList)
		{
			if(s.contains(searchKey))
			{
				newList.add(s);
			}
		}
		
		createReciverList(newList);
		recieverList.refresh();
		
	}
	
	
	@FXML private void onAdd()
	{
		// �й�,�̸�\t�й�,�̸�\t...
		display_reciever.setEditable(true);
		String no = ((Label)recieverList.getSelectionModel().getSelectedItem().getChildren().get(0)).getText();
		String name = ((Label)recieverList.getSelectionModel().getSelectedItem().getChildren().get(1)).getText();
		display_reciever.appendText(no+","+name+"\t");
		display_reciever.setEditable(false);
	}
	
	@SuppressWarnings("unchecked")
	@FXML private void onSendMsg()
    {
		long curTime = System.currentTimeMillis();
		
		String data = display_reciever.getText();
		ArrayList<String> sList = Toolbox.arrToList(data.split("\t"));
      
		JSONObject msg = new JSONObject();
		msg.put("type", NetworkProtocols.MESSAGE_SEND_REQUEST);
		msg.put("sender", uID);
		msg.put("reciever", sList);
		msg.put("msgTitle", msgTitle.getText());
		msg.put("msgContent", msgContent.getText());
		msg.put("sendTime", curTime);
 
		sendProtocol(msg);
	}
	
	@SuppressWarnings("unchecked")
	@FXML private void onNotice()
	{
		JSONObject o = Toolbox.createJSONProtocol(NetworkProtocols.BOARD_LIST_REQUEST);
		o.put("category", "��������");
		sendProtocol(o);	
	}
	
	@SuppressWarnings("unchecked")
	@FXML private void onRequest()
	{
		JSONObject o = Toolbox.createJSONProtocol(NetworkProtocols.BOARD_LIST_REQUEST);
		o.put("category", "���ǻ���");
		sendProtocol(o);
	}
	
	@SuppressWarnings("unchecked")
	@FXML private void onFree()
	{
		JSONObject o = Toolbox.createJSONProtocol(NetworkProtocols.BOARD_LIST_REQUEST);
		o.put("category", "�����Խ���");
		sendProtocol(o);
	}
	
	@FXML private void onBoardSearch()
	{
		System.out.println("�˻� ī�װ��� : "+BOARD_CATEGORY_SELECTOR.getValue());
		System.out.println("�˻� Ű����    : "+BOARD_TITLE_FIELD.getText());
		
		if(BOARD_CATEGORY_SELECTOR.getValue()==null)return;
		
		String[] keys = {"category","search_key"};
		Object[] values = {BOARD_CATEGORY_SELECTOR.getValue(),BOARD_TITLE_FIELD.getText()};
		
		sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.BOARD_SEARCH_REQUEST, keys, values));
	}
	
	@FXML private void onWriteCancel()
	{
		BOARD_WRITE.setVisible(false);
		BOARD_LIST_VIEW.setVisible(true);
	}
	
	@FXML private void onWrite()
	{
		String[] ks = {"�ۼ���","�Խñ�����","�Խñۺ���","ī�װ���"};
		Object[] vs = {uID, BOARD_TITLE.getText(), BOARD_CONTENT.getText(), BOARD_W_CSELECTOR.getValue()};
		
		sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.ENROLL_BOARD_REQUEST, ks, vs));
	}
	
	@FXML private void onCreateBoard()
	{
		BOARD_LIST_VIEW.setVisible(false);
		BOARD_WRITE.setVisible(true);
		BOARD_CONTENT_VIEW.setVisible(false);
	}
	
	@FXML private void onEditBoard()
	{
		
	}
	
	@FXML private void onDeleteBoard()
	{
		
	}
	
	@FXML private void onBackToList()
	{
		BOARD_LIST_VIEW.setVisible(true);
		BOARD_WRITE.setVisible(false);
		BOARD_CONTENT_VIEW.setVisible(false);
	}
	
	//sendmessageList
	public void createdSendedMessageList(JSONArray arr)
	{
		
	}
	
	public void createRecivedmessage(JSONArray arr) // ����
	{
		recievermessageListData.removeAll(recievermessageListData);
	    for(Object t : arr)
	    {
	    	System.out.println(t);
	        JSONObject tt = (JSONObject)t;
	        CheckBox left = new CheckBox();
	        left.setAlignment(Pos.CENTER_LEFT);
	        left.setMaxHeight(Double.MIN_VALUE);
	        left.setMaxWidth(Double.MIN_VALUE);
	        Label Date = new Label(tt.get("�߽Žð�").toString());
	        Date.setAlignment(Pos.CENTER_LEFT);
	        Date.setMaxHeight(30);
	        Date.setMaxWidth(150);
	        Label Center = new Label(tt.get("�߽���").toString());
	        Center.setAlignment(Pos.CENTER_LEFT);
	        Center.setMaxHeight(30);
	        Center.setMaxWidth(150);
	        Label right = new Label(tt.get("�޼�������").toString());
	        right.setAlignment(Pos.CENTER);;
	        right.setMaxHeight(Double.MAX_VALUE);
	        right.setMaxWidth(Double.MAX_VALUE);
	        int no = Integer.parseInt(tt.get("No").toString());
	        Label left2 = new Label(no+"");
	        left2.setAlignment(Pos.CENTER_LEFT);
	        left2.setMaxHeight(30);
	        left2.setMaxWidth(150);
	        left2.setVisible(false);
	        HBox item = new HBox();
	        item.getChildren().addAll(left, left2, Date, Center,right);
	        HBox.setHgrow(right, Priority.ALWAYS);
	        HBox.setHgrow(Date, Priority.ALWAYS);
	        HBox.setHgrow(Center, Priority.ALWAYS);
	        HBox.setHgrow(left, Priority.ALWAYS);
	        item.setAlignment(Pos.CENTER);
	        item.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@SuppressWarnings("unchecked")
			@Override
			public void handle(MouseEvent event) {
						
			if(event.getClickCount()==2)
			{
				JSONObject req = Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_CONTENT_REQUEST);
				req.put("No", no);
				req.put("content_type", "recieve");
				sendProtocol(req);
			}
		}});
	         recievermessageListData.add(item);
	      }
	      messageList.setItems(recievermessageListData);
	   }
	   
	   public void createStudentSearch(JSONArray arr) // ����
	   {
	      StudentListData.removeAll(StudentListData);
	         for(Object tt : arr)
	         {
	        	JSONObject t = (JSONObject)tt;
	        	String str = (t.get("�й�").toString());
	            Label left = new Label(str);
	            left.setAlignment(Pos.CENTER);
	            left.setStyle("-fx-border-color : black");
	            left.setMaxWidth(Double.MAX_VALUE);
	            left.setMaxHeight(Double.MAX_VALUE);
	            String name = t.get("�̸�").toString();
	            Label right = new Label(name);
	            right.setAlignment(Pos.CENTER);
	            right.setMaxWidth(Double.MAX_VALUE);
	            right.setMaxHeight(Double.MAX_VALUE);
	            right.setStyle("-fx-border-color : black");
	            HBox item = new HBox();
	            //item.setMaxSize(Double.MAX_VALUE, maxHeight);
	            item.getChildren().addAll(left, right);
	            HBox.setHgrow(right, Priority.ALWAYS);
	            HBox.setHgrow(left, Priority.ALWAYS);
	            item.setAlignment(Pos.CENTER);
	            EventHandler<MouseEvent> eh = new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						// TODO Auto-generated method stub
						if(event.getClickCount() == 2)
						{
							System.out.println("�Ż����� �� ���� Ŭ�� ");
							JSONObject req = Toolbox.createJSONProtocol(NetworkProtocols.USER_CONTENT_REQUEST);
							req.put("�й�",str);
							req.put("�̸�",name);
							sendProtocol(req);
						}
					}
	            	
				};
				item.setOnMouseClicked(eh);
	            StudentListData.add(item);
	         }
	         
	         StudentList.setItems(StudentListData);
	         StudentList.setVisible(true);
	   }
	   
	   public void createSendMassageList(JSONArray arr)
	   {
		   sendmessageListData.removeAll(sendmessageListData);
		   for(Object t : arr)
		   {
			   System.out.println(t);
			   JSONObject tt = (JSONObject)t;
			   CheckBox left = new CheckBox();
			   left.setAlignment(Pos.CENTER_LEFT);
			   left.setMaxHeight(Double.MIN_VALUE);
			   left.setMaxWidth(Double.MIN_VALUE);
			   Label Date = new Label(tt.get("�߽Žð�").toString());
			   Date.setAlignment(Pos.CENTER_LEFT);
			   Date.setMaxHeight(30);
			   Date.setMaxWidth(150);
			   Label Center = new Label(tt.get("�߽���").toString());
			   Center.setAlignment(Pos.CENTER_LEFT);
			   Center.setMaxHeight(30);
			   Center.setMaxWidth(150);
			   Label right = new Label(tt.get("�޼�������").toString());
			   right.setAlignment(Pos.CENTER);;
			   right.setMaxHeight(Double.MAX_VALUE);
			   right.setMaxWidth(Double.MAX_VALUE);
			   HBox item = new HBox();
			   int no = Integer.parseInt(tt.get("No").toString());
			   Label left2 = new Label(no+"");
			   left2.setVisible(false);
			   left2.setAlignment(Pos.CENTER);
			   left2.setMaxHeight(Double.MAX_VALUE);
			   left2.setMaxWidth(Double.MAX_VALUE);
			   item.getChildren().addAll(left, left2 , Date,Center,right);
			   HBox.setHgrow(right, Priority.ALWAYS);
			   HBox.setHgrow(Date, Priority.ALWAYS);
			   HBox.setHgrow(Center, Priority.ALWAYS);
			   HBox.setHgrow(left, Priority.ALWAYS);
			   item.setAlignment(Pos.CENTER);
			   
			   item.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@SuppressWarnings("unchecked")
					@Override
					public void handle(MouseEvent event) {
						if(event.getClickCount()==2)
						{
							JSONObject req = Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_CONTENT_REQUEST);
							req.put("No", no);
							req.put("content_type", "send");
							sendProtocol(req);
						}
					}
				});
			   
			   sendmessageListData.add(item);
		   }
		   sendmessageList.setItems(sendmessageListData);
	   }
	   
	   public void createStudentManager(ArrayList<String> data)
	   {
	      StudentManagerListData.removeAll(StudentManagerListData);
	      for(String t : data)
	      {
	            String[] info = t.split(",");
	            Label left = new Label(info[0]);
	            left.setAlignment(Pos.CENTER);
	            left.setStyle("-fx-border-color : black");
	            left.setMaxWidth(Double.MAX_VALUE);
	            left.setMaxHeight(Double.MAX_VALUE);
	            Label right = new Label(info[1]);
	            right.setAlignment(Pos.CENTER);
	            right.setMaxWidth(Double.MAX_VALUE);
	            right.setMaxHeight(Double.MAX_VALUE);
	            right.setStyle("-fx-border-color : black");
	            HBox item = new HBox();
	            //item.setMaxSize(Double.MAX_VALUE, maxHeight);
	            item.getChildren().addAll(left, right);
	            HBox.setHgrow(right, Priority.ALWAYS);
	            HBox.setHgrow(left, Priority.ALWAYS);
	            item.setAlignment(Pos.CENTER);
	            StudentManagerListData.add(item);
	      }
	      StudentManagerList.setItems(StudentManagerListData);
	   }
	   
	   @FXML public void onTimerClick()
	   {
		   JSONObject req = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_REQUEST);
		   sendProtocol(req);
	   }
	   
	   @FXML public void onMSGTabClick()
	   {
		   int current = MESSAGE.getSelectionModel().getSelectedIndex();

		   if(saved==current)return;
		   
		   saved = current;
		   
		   switch(saved)
		   {
		   case 0 :
			   		sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_RECIEVE_LIST_REQUEST));
			   break;
		   case 1 :
			   		sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_SEND_LIST_REQUEST));
			   break;
		   case 2 : 
		   		sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_USER_LIST_REQUEST));			   
			   break;
		   }
		   
	   }
	   
	   private void createScheduleBoard_GRID(JSONArray data)
	   {
		   sObjList.clear();
		   schedule_board.getChildren().clear();
		   double startX = 30, startY = 30;
		   double labelStartY = 30;
		   double xGap = 400, yGap = 300;
		   int colCnt = 0;
		   
		   String currentDateTarget = "";
		   boolean changeDate = false;
		   
		   for(Object o : data)
		   {
			   JSONObject target = (JSONObject) o;

			   if(currentDateTarget.equals(Toolbox.getCurrentTimeFormat((java.sql.Timestamp)target.get("�Ͻ�"), "yyyy�� MM�� dd��"))==false)
			   {
				   changeDate = true;
			   }
			   currentDateTarget = Toolbox.getCurrentTimeFormat((java.sql.Timestamp)target.get("�Ͻ�"), "yyyy�� MM�� dd��");
			   
			   ScheduleObject sObj = new ScheduleObject(target.get("�����̸�").toString(), (java.sql.Timestamp)target.get("�Ͻ�"), target.get("�з�").toString(), target.get("����").toString(), ScheduleObject.VIEWTYPE_GRID);
			   sObj.setScheduleID(Integer.parseInt(target.get("������ȣ").toString()));
			   
			   if(colCnt==4)	// ������ü ���� ó��
			   {
				   startX = 30;
				   startY+=yGap;
				   labelStartY = startY + 30 + yGap;
				   colCnt=0;
			   }
			   
			   if(changeDate)	// ��¥ �� ���
			   {
				   Label l = new Label(currentDateTarget+"                  ");
				   l.setLayoutX(30);
				   l.setLayoutY(labelStartY);
				   startY = labelStartY + 90;
				   labelStartY = startY + 30 + yGap;
				   l.setPrefSize(900, 70);
				   l.setFont(Font.font("HYwulM",50));
				   l.setStyle("-fx-background-color: linear-gradient(to bottom, #4c4c4c 0%,#595959 12%,#666666 25%,#474747 39%,#2c2c2c 50%,#000000 51%,#111111 60%,#2b2b2b 76%,#1c1c1c 91%,#131313 100%);");
				   l.setTextFill(Paint.valueOf("white"));
				   l.setVisible(true);
				   schedule_board.getChildren().add(l);
				   changeDate = false;
				   startX = 30;
			   }
			   
			   sObj.setLayoutX(startX);
			   sObj.setLayoutY(startY);
			   sObj.setOnMouseClicked(new EventHandler<MouseEvent>() {
				   @Override
				   public void handle(MouseEvent event) {
					   modifyPanel.setDisable(false);
					   
					   String dateText = sObj.getDateText();
					   String date = dateText.split(" ")[0];
					   
					   modiTitle.setText(sObj.getTitle());
					   modiDate.setPromptText(date);
					   scheduleID.setText(sObj.getScheduleID()+"");
					   // �� �ؽ�Ʈ�� ���� 1111-11-11 11:11:11 �� �Ǿ�����
					   
					   hourPicker.getSelectionModel().select(dateText.split(" ")[1].split(":")[0]);
					   minitePicker.getSelectionModel().select(dateText.split(" ")[1].split(":")[1]);
					   
					   switch(sObj.getCategory())
					   {
					   case "�߿�" : cate_important.setSelected(true);
					   				cate_event.setSelected(false); 
					   				cate_normal.setSelected(false);
					   				break;
					   case "���" : cate_important.setSelected(false);
					   				cate_event.setSelected(true); 
					   				cate_normal.setSelected(false);
					   				break;
					   case "�Ϲ�" : cate_important.setSelected(false);
			   						cate_event.setSelected(false); 
			   						cate_normal.setSelected(true);
			   						break;
					   }
					   
					   contentArea.setText(sObj.getContent().replaceAll("\n", ""));
					   sObj.toFront();
					   modifyPanel.setDisable(true);
					   for(ScheduleObject o : sObjList)
					   {
						   if(o.equals(sObj))
						   {
							   System.out.println("������ ��ü : "+sObj.getTitle());
							   o.selected();
						   }
						   else
						   {
							   o.unSelect();
						   }
					   }
				   }
			   });
			   
			   sObjList.add(sObj);
			   schedule_board.getChildren().add(sObj);
			   startX+=xGap;
			   colCnt++;
		   }
			
	   }
	   
	   private void createScheduleBoard_LIST(JSONArray data)
	   {
		   sObjList.clear();
		   schedule_board.getChildren().clear();
		   
		   double startX = 30, startY = 30;
		   double yGap = 200;
		   boolean changeDate = false;
		   String currentDateTarget = "";
		   for(Object o : data)
		   {
			   JSONObject target = (JSONObject) o;
			   if(currentDateTarget.equals(Toolbox.getCurrentTimeFormat((java.sql.Timestamp)target.get("�Ͻ�"), "yyyy�� MM�� dd��"))==false)
			   {
				   changeDate = true;
			   }
			   currentDateTarget = Toolbox.getCurrentTimeFormat((java.sql.Timestamp)target.get("�Ͻ�"), "yyyy�� MM�� dd��");
			   ScheduleObject sObj = new ScheduleObject(target.get("�����̸�").toString(), (java.sql.Timestamp)target.get("�Ͻ�"), target.get("�з�").toString(), target.get("����").toString(),ScheduleObject.VIEWTYPE_LIST);
			   sObj.setScheduleID(Integer.parseInt(target.get("������ȣ").toString()));
			   
			   if(changeDate)
			   {
				   System.out.println("���ں��� : "+currentDateTarget);
				   Label l = new Label(currentDateTarget+"                  ");
				   l.setLayoutX(30);
				   l.setLayoutY(startY);
				   startY+=90;
				   l.setPrefSize(900, 70);
				   l.setFont(Font.font("HYwulM",50));
				   l.setStyle("-fx-background-color: linear-gradient(to bottom, #4c4c4c 0%,#595959 12%,#666666 25%,#474747 39%,#2c2c2c 50%,#000000 51%,#111111 60%,#2b2b2b 76%,#1c1c1c 91%,#131313 100%);");
				   l.setTextFill(Paint.valueOf("white"));
				   l.setVisible(true);
				   schedule_board.getChildren().add(l);
				   changeDate = false;
			   }
			   
			   sObj.setLayoutX(startX);
			   sObj.setLayoutY(startY);
			   startY+=yGap;
			   sObj.setOnMouseClicked(new EventHandler<MouseEvent>() {
				   @Override
				   public void handle(MouseEvent event) {
					   modifyPanel.setDisable(false);
					   
					   String dateText = sObj.getDateText();
					   String date = dateText.split(" ")[0];
					   
					   modiTitle.setText(sObj.getTitle());
					   modiDate.setPromptText(date);
					   scheduleID.setText(sObj.getScheduleID()+"");
					   // �� �ؽ�Ʈ�� ���� 1111-11-11 11:11:11 �� �Ǿ�����
					   
					   hourPicker.getSelectionModel().select(dateText.split(" ")[1].split(":")[0]);
					   minitePicker.getSelectionModel().select(dateText.split(" ")[1].split(":")[1]);
					   
					   switch(sObj.getCategory())
					   {
					   case "�߿�" : cate_important.setSelected(true);
					   				cate_event.setSelected(false); 
					   				cate_normal.setSelected(false);
					   				break;
					   case "���" : cate_important.setSelected(false);
					   				cate_event.setSelected(true); 
					   				cate_normal.setSelected(false);
					   				break;
					   case "�Ϲ�" : cate_important.setSelected(false);
			   						cate_event.setSelected(false); 
			   						cate_normal.setSelected(true);
			   						break;
					   }
					   
					   contentArea.setText(sObj.getContent().replaceAll("\n", ""));
					   sObj.toFront();
					   modifyPanel.setDisable(true);
					   for(ScheduleObject o : sObjList)
					   {
						   if(o.equals(sObj))
						   {
							   System.out.println("������ ��ü : "+sObj.getTitle());
							   o.selected();
						   }
						   else
						   {
							   o.unSelect();
						   }
					   }
				   }
			   });
			   
			   sObjList.add(sObj);
			   schedule_board.getChildren().add(sObj);
			   
		   }
		   

	   }
	   
		public void createScheduleBoard_CELL(JSONArray data, String startDate)
		{
			sObjList.clear();
		    schedule_board.getChildren().clear();
		   
		   for(Object o : data)
		   {
			   JSONObject target = (JSONObject) o;
			   ScheduleObject sObj = new ScheduleObject(target.get("�����̸�").toString(), (java.sql.Timestamp)target.get("�Ͻ�"), target.get("�з�").toString(), target.get("����").toString(),ScheduleObject.VIEWTYPE_CELL);
			   sObj.setScheduleID(Integer.parseInt(target.get("������ȣ").toString()));
			   sObj.setOnMouseClicked(new EventHandler<MouseEvent>() {
				   @Override
				   public void handle(MouseEvent event) {
					   modifyPanel.setDisable(false);
					   
					   String dateText = sObj.getDateText();
					   String date = dateText.split(" ")[0];
					   
					   modiTitle.setText(sObj.getTitle());
					   modiDate.setPromptText(date);
					   scheduleID.setText(sObj.getScheduleID()+"");
					   
					   hourPicker.getSelectionModel().select(dateText.split(" ")[1].split(":")[0]);
					   minitePicker.getSelectionModel().select(dateText.split(" ")[1].split(":")[1]);
					   
					   switch(sObj.getCategory())
					   {
					   case "�߿�" : cate_important.setSelected(true);
					   				cate_event.setSelected(false); 
					   				cate_normal.setSelected(false);
					   				break;
					   case "���" : cate_important.setSelected(false);
					   				cate_event.setSelected(true); 
					   				cate_normal.setSelected(false);
					   				break;
					   case "�Ϲ�" : cate_important.setSelected(false);
			   						cate_event.setSelected(false); 
			   						cate_normal.setSelected(true);
			   						break;
					   }
					   
					   contentArea.setText(sObj.getContent().replaceAll("\n", ""));
					   sObj.toFront();
					   modifyPanel.setDisable(true);
					   for(ScheduleObject o : sObjList)
					   {
						   if(o.equals(sObj))
						   {
							   System.out.println("������ ��ü : "+sObj.getTitle());
							   o.selected();
						   }
						   else
						   {
							   o.unSelect();
						   }
					   }
				   }
			   });
			   sObjList.add(sObj);
		   }
		   
		   CalendarObject calendar = new CalendarObject(sObjList);
		   DatePicker pick = calendar.pick;
		   pick.setOnAction(new EventHandler<ActionEvent>() {
				@SuppressWarnings("unchecked")
				@Override
				public void handle(ActionEvent event) {
					calendar.getDataInstance().getCalander().setTime(Toolbox.StringToDate(pick.getValue().getYear()+""+pick.getValue().getMonthValue()+""+pick.getValue().getDayOfMonth(), "yyyyMd"));
					calendar.DateHeader.setText(new SimpleDateFormat("yyyy�� M��").format(Toolbox.StringToDate(pick.getValue().getYear()+""+pick.getValue().getMonthValue()+""+pick.getValue().getDayOfMonth(), "yyyyMd")));
					JSONObject req = Toolbox.createJSONProtocol(NetworkProtocols.MONTHLY_SCHEDULE_VIEW_REQUEST);
					req.put("startDate", calendar.getDataInstance().getStartDateOfMonth());
					req.put("endDate", calendar.getDataInstance().getEndDateOfMonth());
//						calendar.initDate();
					sendProtocol(req);
				}
			});
		   calendar.setLayoutX(100);
		   calendar.setLayoutY(30);
		   schedule_board.getChildren().add(calendar);
		}

	   
	   private void createScheduleBoard_CELL(JSONArray data)
	   {
		   sObjList.clear();
		   schedule_board.getChildren().clear();
		   
		   for(Object o : data)
		   {
			   JSONObject target = (JSONObject) o;
			   ScheduleObject sObj = new ScheduleObject(target.get("�����̸�").toString(), (java.sql.Timestamp)target.get("�Ͻ�"), target.get("�з�").toString(), target.get("����").toString(),ScheduleObject.VIEWTYPE_CELL);
			   sObj.setScheduleID(Integer.parseInt(target.get("������ȣ").toString()));
			   sObj.setOnMouseClicked(new EventHandler<MouseEvent>() {
				   @Override
				   public void handle(MouseEvent event) {
					   modifyPanel.setDisable(false);
					   
					   String dateText = sObj.getDateText();
					   String date = dateText.split(" ")[0];
					   
					   modiTitle.setText(sObj.getTitle());
					   modiDate.setPromptText(date);
					   scheduleID.setText(sObj.getScheduleID()+"");
					   // �� �ؽ�Ʈ�� ���� 1111-11-11 11:11:11 �� �Ǿ�����
					   
					   hourPicker.getSelectionModel().select(dateText.split(" ")[1].split(":")[0]);
					   minitePicker.getSelectionModel().select(dateText.split(" ")[1].split(":")[1]);
					   
					   switch(sObj.getCategory())
					   {
					   case "�߿�" : cate_important.setSelected(true);
					   				cate_event.setSelected(false); 
					   				cate_normal.setSelected(false);
					   				break;
					   case "���" : cate_important.setSelected(false);
					   				cate_event.setSelected(true); 
					   				cate_normal.setSelected(false);
					   				break;
					   case "�Ϲ�" : cate_important.setSelected(false);
			   						cate_event.setSelected(false); 
			   						cate_normal.setSelected(true);
			   						break;
					   }
					   
					   contentArea.setText(sObj.getContent().replaceAll("\n", ""));
					   sObj.toFront();
					   modifyPanel.setDisable(true);
					   for(ScheduleObject o : sObjList)
					   {
						   if(o.equals(sObj))
						   {
							   System.out.println("������ ��ü : "+sObj.getTitle());
							   o.selected();
						   }
						   else
						   {
							   o.unSelect();
						   }
					   }
				   }
			   });
			   sObjList.add(sObj);
		   }
		   
		   CalendarObject calendar = new CalendarObject(sObjList);
		   DatePicker pick = calendar.pick;
		   pick.setOnAction(new EventHandler<ActionEvent>() {
				@SuppressWarnings("unchecked")
				@Override
				public void handle(ActionEvent event) {
					calendar.getDataInstance().getCalander().setTime(Toolbox.StringToDate(pick.getValue().getYear()+""+pick.getValue().getMonthValue()+""+pick.getValue().getDayOfMonth(), "yyyyMd"));
					calendar.DateHeader.setText(new SimpleDateFormat("yyyy�� M��").format(Toolbox.StringToDate(pick.getValue().getYear()+""+pick.getValue().getMonthValue()+""+pick.getValue().getDayOfMonth(), "yyyyMd")));
					JSONObject req = Toolbox.createJSONProtocol(NetworkProtocols.MONTHLY_SCHEDULE_VIEW_REQUEST);
					req.put("startDate", calendar.getDataInstance().getStartDateOfMonth());
					req.put("endDate", calendar.getDataInstance().getEndDateOfMonth());
//					calendar.initDate();
					sendProtocol(req);
				}
			});
		   calendar.setLayoutX(100);
		   calendar.setLayoutY(30);
		   schedule_board.getChildren().add(calendar);
	   }
	   
	   @FXML public void onSchedulModify()
	   {
		   modifyPanel.setDisable(false);
	   }
	   
	   @FXML public void onModifySumbit()
	   {
		   String date = "";
		   if(modiDate.getValue()==null)
		   {
			   if(modiDate.getPromptText().length()==0)
			   {
				   CustomDialog.showMessageDialog("��¥�� �������ּ���!", sManager.getStage());
				   return;
			   }
			   else
			   {
				   date = modiDate.getPromptText();				   
			   }
		   }
		   else
		   {
			   date = modiDate.getValue().toString();
		   }
		   
		   if(hourPicker.getValue()==null||minitePicker.getValue()==null)
		   {
			   CustomDialog.showMessageDialog("�ð��� �������ּ���!", sManager.getStage());
			   return;
		   }
		   
		   date = date + " " + hourPicker.getValue() + ":" + minitePicker.getValue();
		   
		   String cate = "";
	
		   if(cate_important.isSelected()==true)
		   {
			   cate = "�߿�";
		   }
		   
		   if(cate_event.isSelected()==true)
		   {
			   cate = "���";
		   }
		   
		   if(cate_normal.isSelected()==true)
		   {
			   cate = "�Ϲ�";
		   }
		   
		   String[] keys = {"������ȣ","�����̸�","�Ͻ�","�з�","����","������","������","viewtype"};
		   Object[] values = {scheduleID.getText(), modiTitle.getText(), date, cate, contentArea.getText(), startDateString, endDateString, SCHEDULE_DISPLAY_MODE};
		   System.out.println(startDateString+", "+endDateString);
		   sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.MODIFY_SCHEDULE_REQUEST, keys, values));
		   
	   }
	   
	   @SuppressWarnings("unchecked")
	   @FXML public void createNewSchedule()
	   {
		   CustomDialog dlg = new CustomDialog(Statics.SCHEDULE_CREATE_DIALOG, Statics.SCHEDULE_CREATE_DIALOG_TITLE, sManager.getStage());
		   ScheduleCreateDialogController con = (ScheduleCreateDialogController) dlg.getController();
		   con.setWindow(dlg);
		   dlg.showAndWait();
		   
		   if(dlg.getUserData().equals("cancel"))
		   {
			   return;
		   }
		   
		   JSONObject obj = (JSONObject)dlg.getUserData();
		   obj.put("type", NetworkProtocols.ADD_SCHEDULE_REQUEST);
		   obj.put("������", startDateString);
		   obj.put("������", endDateString);
		   obj.put("viewtype", SCHEDULE_DISPLAY_MODE);
		   sendProtocol(obj);
	   }
	   
	   @SuppressWarnings("unchecked")
	   @FXML public void deleteSchedule()
	   {
		   for(ScheduleObject o : sObjList)
		   {
			   if(o.isSelected()==true)
			   {
				   JSONObject obj = Toolbox.createJSONProtocol(NetworkProtocols.DELETE_SCHEDULE_REQUEST);
				   obj.put("������ȣ", o.getScheduleID());
				   obj.put("�Ͻ�", o.getDateText());
				   obj.put("������", startDateString);
				   obj.put("������", endDateString);
				   obj.put("viewtype", SCHEDULE_DISPLAY_MODE);
				   sendProtocol(obj);
				   return;
			   }
		   }
	   }
	   
	   @FXML public void onProfessionalSearch()
	   {
		   String[] keys = {"������", "������", "�з�"};
		   
		   String duration = "";
		   
		   if(search_daily.isSelected()==true)
		   {
			   duration = "�߿�";
		   }
		   else if(search_weekly.isSelected()==true)
		   {
			   duration = "���";			   
		   }
		   else if(search_monthly.isSelected()==true)
		   {
			   duration = "�Ϲ�";   
		   }
		   else
		   {
			   duration = "��ü";
		   }
		   
		   Object[] values = {startDate.getValue().toString(), endDate.getValue().toString(), duration};
		 
		   sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.SCHEDULE_PROFESSIONAL_SEARCH_REQUEST, keys, values));
		   
	   }
	   
	   @FXML public void onViewMode()
	   {
		   if(SCHEDULE_DISPLAY_DATA==null)return;
		   
		   SCHEDULE_DISPLAY_MODE = schedule_view_mode.getValue();
		   System.out.println("������ �� : "+SCHEDULE_DISPLAY_MODE);
		   if(SCHEDULE_DISPLAY_MODE.equals("�׸�����"))
		   {
				createScheduleBoard_GRID(SCHEDULE_DISPLAY_DATA);										
		   }
		   else if(SCHEDULE_DISPLAY_MODE.equals("����Ʈ���"))
		   {
			   createScheduleBoard_LIST(SCHEDULE_DISPLAY_DATA);
		   }
		   else if(SCHEDULE_DISPLAY_MODE.equals("�޷¸��"))
		   {
			   createScheduleBoard_CELL(SCHEDULE_DISPLAY_DATA);
		   }
	   }
	   
	   public void createWeabakManager(JSONArray arr)  // ����
	   {
	      StudentWeabakListData.removeAll(StudentWeabakListData);
	      for(Object a : arr)
	      {
	    	JSONObject json = (JSONObject)a;
	    	int No = Integer.parseInt(json.get("�ܹڹ�ȣ").toString());
	    	
	    	int check = Integer.parseInt(json.get("���ο���").toString());
	    	String acceptcheck;
	    	if(check == 0)
	    	{
	    		acceptcheck = "�����";
	    	}
	    	else if(check == 1)
	    	{
	    		acceptcheck = "����";
	    	}
	    	else
	    	{
	    		acceptcheck = "����";
	    	}
	    	
	    	String date = json.get("��û����").toString().split(" ")[0];
	        Label left1 = new Label(date);
	        left1.setAlignment(Pos.CENTER);
	        left1.setMaxHeight(30);
	        left1.setMaxWidth(200);
	        Label left2 = new Label(json.get("�ܹ�����").toString());
	        left2.setAlignment(Pos.CENTER);
	        left2.setMaxHeight(30);
	        left2.setMaxWidth(200);
	        String num = json.get("�й�").toString();
	        Label left3 = new Label(num);
	        left3.setAlignment(Pos.CENTER_RIGHT);
	        left3.setMaxHeight(30);
	        left3.setMaxWidth(150);
	        String name = json.get("�̸�").toString();
	        Label left4 = new Label(name);
	        left4.setAlignment(Pos.CENTER);
	        left4.setMaxHeight(30);
	        left4.setMaxWidth(220);
	        Label center = new Label(json.get("����").toString());
	        center.setAlignment(Pos.CENTER);
	        center.setMaxHeight(Double.MAX_VALUE);
	        center.setMaxWidth(Double.MAX_VALUE);
	        Label right = new Label(json.get("������").toString());
	        right.setAlignment(Pos.CENTER);
	        right.setMaxHeight(30);
	        right.setMaxWidth(270);
	        Label right1 = new Label(acceptcheck);
	        right1.setAlignment(Pos.CENTER);
	        right1.setMaxHeight(30);
	        right1.setMaxWidth(170);
	        HBox item = new HBox();
	        item.getChildren().addAll(left1,left2,left3,left4,center,right,right1);
	        HBox.setHgrow(left1, Priority.ALWAYS);
	        HBox.setHgrow(left2, Priority.ALWAYS);
	        HBox.setHgrow(left3, Priority.ALWAYS);
	        HBox.setHgrow(left4, Priority.ALWAYS);
	        HBox.setHgrow(center, Priority.ALWAYS);
	        HBox.setHgrow(right, Priority.ALWAYS);
	        HBox.setHgrow(right1, Priority.ALWAYS);
	        item.setAlignment(Pos.CENTER);
	        StudentWeabakListData.add(item);
	        
	        EventHandler<MouseEvent> eh = new EventHandler<MouseEvent>() {

				@SuppressWarnings("unchecked")
				@Override
				public void handle(MouseEvent event) {
					if(event.getClickCount() == 2)
					{
						JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_CONTENT_REQUEST);
						json.put("No", No);
						json.put("�й�", num);
						json.put("�̸�", name);
						sendProtocol(json);
					}
				}
			};
			item.setOnMouseClicked(eh);
	      }
	      
	      StudentWeabakList.setItems(StudentWeabakListData);
	      StudentWeabakList.setVisible(true);
	      
	   }
	   
	   public void createPlusMinusSearch(JSONArray arr)  // ����
	   {
	      StudentManagerListData.removeAll(StudentManagerListData);
	 
	      for(Object a : arr)
	      {
	    	JSONObject json = (JSONObject)a;
	        Label left = new Label(json.get("�й�").toString());
	        left.setAlignment(Pos.CENTER);
	        left.setMaxHeight(30);
	        left.setMaxWidth(300);
	        Label left1 = new Label(json.get("�̸�").toString());
	        left1.setAlignment(Pos.CENTER);
	        left1.setMaxHeight(30);
	        left1.setMaxWidth(310);
	        Label center = new Label(json.get("����").toString());
	        center.setAlignment(Pos.CENTER);
	        center.setMaxHeight(Double.MAX_VALUE);
	        center.setMaxWidth(Double.MAX_VALUE);
	        Label center1 = new Label(json.get("����").toString());
	        center1.setAlignment(Pos.CENTER);
	        center1.setMaxHeight(Double.MAX_VALUE);
	        center1.setMaxWidth(Double.MAX_VALUE);
	        Label right = new Label(json.get("�հ�").toString());
	        right.setAlignment(Pos.CENTER);
	        right.setMaxHeight(Double.MAX_VALUE);
	        right.setMaxWidth(Double.MAX_VALUE);
	        HBox item = new HBox();
	        item.getChildren().addAll(left,left1,center,center1,right);
	        HBox.setHgrow(left, Priority.ALWAYS);
	        HBox.setHgrow(left1, Priority.ALWAYS);
	        HBox.setHgrow(center, Priority.ALWAYS);
	        HBox.setHgrow(center1, Priority.ALWAYS);
	        HBox.setHgrow(right, Priority.ALWAYS);
	        item.setAlignment(Pos.CENTER);
	        StudentManagerListData.add(item);
	         
	      }
	      StudentManagerList.setItems(StudentManagerListData);
	      StudentManagerList.setVisible(true);
	      
	   }
	   
	public void StudentManagerList(JSONArray arr)
	   {
		   StudentMenagerListCheckData.removeAll(StudentMenagerListCheckData);
		   System.out.println("�޼ҵ� ����");
		   for(Object a : arr)
		   {
		    JSONObject json = (JSONObject)a;
		    Label left = new Label(json.get("��¥").toString());
		    left.setAlignment(Pos.CENTER);
		    left.setMaxHeight(30);
		    left.setMaxWidth(260);
		    Label left1 = new Label(json.get("�й�").toString());
		    left1.setAlignment(Pos.CENTER);
		    left1.setMaxHeight(30);
		    left1.setMaxWidth(233);
		    Label center = new Label(json.get("�̸�").toString());
		    center.setAlignment(Pos.CENTER);
		    center.setMaxHeight(30);
		    center.setMaxWidth(257);
		    Label center1 = new Label(json.get("�����Ÿ��").toString());
		    center1.setAlignment(Pos.CENTER);
		    center1.setMaxHeight(30);
		    center1.setMaxWidth(247);
		    Label right = new Label(json.get("����").toString());
		    right.setAlignment(Pos.CENTER);
		    right.setMaxHeight(30);
		    right.setMaxWidth(241);
		    Label right1 = new Label(json.get("����").toString());
		    right1.setAlignment(Pos.CENTER);
		    right1.setMaxHeight(30);
		    right1.setMaxWidth(581);
		    HBox item = new HBox();
		    item.getChildren().addAll(left,left1,center,center1,right,right1);
		    HBox.setHgrow(left, Priority.ALWAYS);
		    HBox.setHgrow(left1, Priority.ALWAYS);
		    HBox.setHgrow(center, Priority.ALWAYS);
		    HBox.setHgrow(center1, Priority.ALWAYS);
		    HBox.setHgrow(right, Priority.ALWAYS);
		    HBox.setHgrow(right1, Priority.ALWAYS);
		    item.setAlignment(Pos.CENTER);
		    StudentMenagerListCheckData.add(item);
		         
		    }
		    StudentMenagerListCheck.setItems(StudentMenagerListCheckData);
		    StudentMenagerListCheck.setVisible(true);
	   }
	
	@FXML private void onSearch1()
	{
		String searchKey = studenttextField.getText();
		JSONArray array = new JSONArray();
		
		
		for(Object s : jarray)
		{
			JSONObject t = (JSONObject)s;
		    if(t.get("�й�").toString().contains(searchKey) || t.get("�̸�").toString().contains(searchKey))
		    {
		    	array.add(t);
		    }
		}
		createStudentSearch(array);
		StudentList.refresh();
		
	}
	
	//���� �߰�
	@FXML private void onSearch2()
	{
		String searchKey = WeabakField.getText();
		JSONArray jarr = new JSONArray();
		
		for(Object a : jarray)
		{
			JSONObject aa = (JSONObject)a;
			if((aa.get("�й�").toString().contains(searchKey)) ||(aa.get("�̸�").toString().contains(searchKey)) || aa.get("��û����").toString().contains(searchKey) 
					|| aa.get("�ܹ�����").toString().contains(searchKey))
			{
				jarr.add(aa);
			}			
		}
		createWeabakManager(jarr);
		StudentWeabakList.refresh();
		
	}
	@FXML private void onSearch3()
	{
		String searchKey = PMField.getText();
		System.out.println("�ܹ� �л� ã�� ����");
		JSONArray jarr = new JSONArray();
		
		for(Object a : jarray)
		{
			JSONObject aa = (JSONObject)a;
			if((aa.get("�й�").toString().contains(searchKey)) ||(aa.get("�̸�").toString().contains(searchKey)))
			{
				jarr.add(aa);
			}			
		}
		createPlusMinusSearch(jarr);
		StudentManagerList.refresh();
	}
	
	   @FXML public void onWeabakTabClick()
	   {
		   int select = STUDNETMANAGER.getSelectionModel().getSelectedIndex();
		   
		   if(saved == select) return;
		   
		   saved = select;
		   
		   if(saved == 0)
		   {
			   JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_INFO_TAP_REQUEST);
			   json.put("category", "main");
			   sendProtocol(json);
		   }
		   else if(saved == 1)
		   {
			   sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.PLUS_MINUS_TAP_REQUEST));
		   }
		   else
		   {
			   sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.PLUS_MINUS_TAP_INFO_REQUEST));
			   System.out.println("������Ʈ ����");
		   }
	   }
	   
		@FXML private void onWait()
		{
			JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_INFO_TAP_REQUEST);
			json.put("category", "�����");
			sendProtocol(json);
		}
		
		@FXML private void onAccept()
		{
			JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_INFO_TAP_REQUEST);
			json.put("category", "���οܹ�");
			sendProtocol(json);
		}
		
		@FXML private void onFalse()
		{
			JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_INFO_TAP_REQUEST);
			json.put("category", "����οܹ�");
			sendProtocol(json);
		}
}