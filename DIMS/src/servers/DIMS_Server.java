package servers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.orsoncharts.util.json.JSONArray;
import com.orsoncharts.util.json.JSONObject;

import clients.customcontrols.CalendarObject;
import clients.customcontrols.CalendarObject.CalendarDataManager;
import clients.customcontrols.ScheduleObject;
import databases.DatabaseHandler;
import tools.NetworkProtocols;
import tools.Toolbox;

public class DIMS_Server {
	
	private ServerSocket server;
	private static boolean SERVER_RUN = false;
	private boolean PRINT_LOG = true;
	private DatabaseHandler handler;
	private ArrayList<ConnectedClient> clients;
	
	
	public DIMS_Server()
	{
		
		clients = new ArrayList<ConnectedClient>();
		//new Controller().start();
		//serverOpen();
		
	}
	
	public void serverOpen()
	{
		try
		{
			server = new ServerSocket(8080);
			if(PRINT_LOG)
			{
				System.out.println("[Server] ���� ����");
			}
			handler = new DatabaseHandler();
			if(PRINT_LOG) System.out.println("[Server] �����ͺ��̽��� ���� �õ�...");
			
			int result = handler.connect();
			
			switch(result)
			{
			case DatabaseHandler.DRIVER_INIT_ERROR : if(PRINT_LOG)System.out.println("[Server] JDBC����̹� ������ �߸��ƽ��ϴ�."); return;
			case DatabaseHandler.LOGIN_FAIL_ERROR : if(PRINT_LOG)System.out.println("[Server] �����ͺ��̽��� �α������� ���߽��ϴ�. ���̵� �Ǵ� ��й�ȣ�� Ȯ���ϼ���"); return;
			case DatabaseHandler.COMPLETE : if(PRINT_LOG)System.out.println("[Server] ���� ����");
											SERVER_RUN = true;
			}
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if(PRINT_LOG)System.out.println("[Server] Waiter ������ ����");
		new Waiter().start();
	}
	
	class Waiter extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while(SERVER_RUN)
				{
					if(PRINT_LOG)System.out.println("\t[Waiter] Ŭ���̾�Ʈ�� ��ٸ��� ���Դϴ�...");
					Socket newClient = server.accept();
					if(PRINT_LOG)System.out.println("\t[Waiter] ���ο� Ŭ���̾�Ʈ ����, Connector ������ ����");
					new Connector(newClient).start();

				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally {
				SERVER_RUN = false;
			}
		}
	}
	
	class Connector extends Thread
	{
		Socket client;
		String userName = "unknown";
		String userIdentify = "";
		ObjectInputStream fromClient;
		ObjectOutputStream toClient;
		
		Connector(Socket client)
		{
			this.client = client;
			try
			{
				fromClient = new ObjectInputStream(client.getInputStream());
				toClient = new ObjectOutputStream(client.getOutputStream());
				if(PRINT_LOG)System.out.println("\t\t[Connector-"+userName+"] ��Ʈ�� ���� �Ϸ�");
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run()
		{
			try
			{
				while(SERVER_RUN)
				{
					JSONObject request = null;
					try
					{
						request = (JSONObject)fromClient.readObject();
						if(request==null)
						{
							if(PRINT_LOG)System.out.println("\t\t["+userName+"] ����� ����, ������ ����");
							break;
						}
						
					}
					catch(ClassNotFoundException e)
					{
						JSONObject respond = new JSONObject();
						respond.put("type", NetworkProtocols.INVALID_REQUEST_ERROR);
						sendProtocol(respond);
						continue;
					}
					
					String type = request.get("type").toString();
					if(PRINT_LOG)System.out.println("\t\t["+userName+"] request type : "+type);
					
					if(type.equals(NetworkProtocols.LOGIN_REQUEST))
					{
						String reqID = request.get("id").toString();
						String reqPassword = request.get("password").toString();
						
						if(PRINT_LOG)System.out.println("\t\t\t[request-"+userName+"] LOGIN_REQUEST, ID : "+reqID+", Password : "+reqPassword);
						ResultSet s = handler.excuteQuery("select * from ����� where �й�='"+reqID+"'");
						
						if(s.next())
						{
							String realPassword = s.getString("��й�ȣ");
							if(realPassword.equals(reqPassword))
							{
								userName = s.getString("�̸�");
								userIdentify = reqID;
								if(PRINT_LOG)System.out.println("\t\t\t[Server] LOGIN_ACCEPT");
								
								JSONObject respond = new JSONObject();
								respond.put("type", NetworkProtocols.LOGIN_ACCEPT);
								respond.put("user_level", s.getString("����ڵ��"));
								sendProtocol(respond);
								/* �� ��������  HashMap�� �־������ */
								clients.add(new ConnectedClient(userIdentify, userName, s.getString("����ڵ��").toString(), toClient));
							}
							else
							{
								if(PRINT_LOG)System.out.println("\t\t\t[Server] LOGIN_DENY, Incorrect Password, requset : "+reqPassword+", in database : "+realPassword);
								JSONObject respond = new JSONObject();
								respond.put("type", NetworkProtocols.LOGIN_DENY);
								
								sendProtocol(respond);
							}
						}
						else
						{
							if(PRINT_LOG)System.out.println("\t\t\t[Server] LOGIN_DENY, Not Exist User requset : "+reqID);
							JSONObject respond = new JSONObject();
							respond.put("type", NetworkProtocols.LOGIN_DENY);
							
							sendProtocol(respond);
						}
						s.close();
					}
					else if(type.equals(NetworkProtocols.ID_DUP_CHECK_REQUEST))
					{
						String reqID = request.get("id").toString();
						if(PRINT_LOG)System.out.println("\t\t\t[request-"+userName+"] ID_DUP_CHECK_REQUEST, "+reqID);
						
						ResultSet s = handler.excuteQuery("select * from ����� where �й�='"+reqID+"'");
						
						
						if(s.next())
						{
							if(PRINT_LOG)System.out.println("\t\t\t[Server] ID_DUP_RESPOND_DENY");
							sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.ID_DUP_RESPOND_DENY));
						}
						else
						{
							if(PRINT_LOG)System.out.println("\t\t\t[Server] ID_DUP_RESPOND_OK");
							sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.ID_DUP_RESPOND_OK));
						}
						
						s.close();
					}
					else if(type.equals(NetworkProtocols.EXIT_REQUEST))
					{
						sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.EXIT_RESPOND));
						
						while(true)
						{
							sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.RECIEVE_READY));
							JSONObject r = null;
							try
							{
								r = (JSONObject)fromClient.readObject();
							}
							catch (ClassNotFoundException e)
							{
								e.printStackTrace();
							}
							
							if(r.get("type").equals(NetworkProtocols.RECIEVE_READY_OK))
							{
								sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.PLZ_REQUEST));
								break;
							}
						}
					}
					else if(type.equals(NetworkProtocols.WINDOW_INIT_PROPERTY))
					{
						if(PRINT_LOG)System.out.println("\t\t["+userName+"] window_init_request");
						
						JSONObject respond = new JSONObject();
						respond.put("type", NetworkProtocols.WINDOW_INIT_PROPERTY);
						respond.put("uID", userIdentify);
						respond.put("uName", userName);
						sendProtocol(respond);
						
						if(PRINT_LOG)System.out.println("\t\t\t[Server] send property");
					}
					else if(type.equals(NetworkProtocols.ENROLL_BOARD_REQUEST))
					{
						if(PRINT_LOG)System.out.println(request.toJSONString());
						
						String creator = (String)request.get("�ۼ���");
						String title = (String)request.get("�Խñ�����");
						String content = (String)request.get("�Խñۺ���");
						String category = (String)request.get("ī�װ���");
						
						if(creator==null||title.length()==0||content.length()==0||category==null)
						{
							toClient.writeObject(Toolbox.createJSONProtocol(NetworkProtocols.ENROLL_BOARD_ERROR));
							toClient.flush();
							return;
						}
						
						String qry = "insert into �Խñ�(�ۼ���,�Խñ�����,�Խñۺ���,ī�װ���,�ۼ�����) values('"+creator+"','"+title+"','"+content+"','"+category+"',now())";
						
						handler.excuteUpdate(qry);
						if(PRINT_LOG)System.out.println("\t\t\t["+userName+"] "+request.toJSONString());
						
						sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.ENROLL_BOARD_RESPOND));
					}
					else if(type.equals(NetworkProtocols.BOARD_LIST_REQUEST))
					{
						ResultSet rs = handler.excuteQuery("select G.�Խñ۹�ȣ, S.�̸�, G.�Խñ�����, G.�ۼ����� from ����� S, �Խñ� G "
								                         + "where S.�й�=G.�ۼ��� and G.ī�װ���='"+request.get("category").toString()+"';");
						JSONArray arr = new JSONArray();
						String[] keys = {"No","�̸�", "�Խñ�����", "�ۼ�����"};
						while(rs.next())
						{
							Object[] o = {rs.getInt("�Խñ۹�ȣ"),
										  rs.getString("�̸�"),
									      rs.getString("�Խñ�����"),
									      rs.getDate("�ۼ�����")}; 
							
							JSONObject n = Toolbox.createJSONProtocol(keys, o);
							arr.add(n);								
						}
						JSONObject res = Toolbox.createJSONProtocol(NetworkProtocols.BOARD_LIST_RESPOND);
						res.put("board_list", arr);
						sendProtocol(res);
						
					}
					else if(type.equals(NetworkProtocols.BOARD_CONTENT_REQUEST))
					{
						int reqno = (int)request.get("No");
						String qry = "select S.�̸�, G.�Խñ�����, G.�Խñۺ���, G.ī�װ���, G.�ۼ����� from ����� S, �Խñ� G"
								+    " where S.�й�=G.�ۼ��� and G.�Խñ۹�ȣ="+reqno+";";
						ResultSet rs = handler.excuteQuery(qry);
						while(rs.next())
						{
							String[] keys = {"�̸�","�Խñ�����","�Խñۺ���","ī�װ���","�ۼ�����"};
							Object[] values = {rs.getString("�̸�"),rs.getString("�Խñ�����"),rs.getString("�Խñۺ���"),rs.getString("ī�װ���"),rs.getDate("�ۼ�����")};
							
							sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.BOARD_CONTENT_RESPOND, keys, values));
						}
					}
					else if(type.equals(NetworkProtocols.BOARD_SEARCH_REQUEST))
					{
						String category = request.get("category").toString();
						
						ResultSet rs = null;
						if(category=="��ü")
						{
							rs = handler.excuteQuery("select G.�Խñ۹�ȣ, S.�̸�, G.�Խñ�����, G.�ۼ����� from ����� S, �Խñ� G "
			                         + "where S.�й�=G.�ۼ��� and G.�Խñ����� like '%"+request.get("search_key").toString()+"%';");	
						}
						else
						{
							rs = handler.excuteQuery("select G.�Խñ۹�ȣ, S.�̸�, G.�Խñ�����, G.�ۼ����� from ����� S, �Խñ� G "
			                         + "where S.�й�=G.�ۼ��� and G.�Խñ����� like '%"+request.get("search_key").toString()+"%' and G.ī�װ��� = '"+category+"';");
						}
						
						if(Toolbox.getResultSetSize(rs)==0)
						{
							sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.BOARD_NO_SEARCH_RESULT));
							continue;
						}
						
						JSONArray arr = new JSONArray();
						String[] keys = {"No","�̸�", "�Խñ�����", "�ۼ�����"};
						while(rs.next())
						{
							Object[] o = {rs.getInt("�Խñ۹�ȣ"),
										  rs.getString("�̸�"),
									      rs.getString("�Խñ�����"),
									      rs.getDate("�ۼ�����")}; 
							
							JSONObject n = Toolbox.createJSONProtocol(keys, o);
							arr.add(n);								
						}
						JSONObject res = Toolbox.createJSONProtocol(NetworkProtocols.BOARD_SEARCH_RESPOND);
						res.put("boardlist", arr);
						sendProtocol(res);
					}
					else if(type.equals(NetworkProtocols.MESSAGE_USER_LIST_REQUEST))
					{
						String qry = "select �й�, �̸� from �����;";
						ResultSet rs = handler.excuteQuery(qry);
						
						ArrayList<String> rcList = new ArrayList<String>();
						
						while(rs.next())
						{
							rcList.add(rs.getString("�й�")+","+rs.getString("�̸�"));
						}
						
						JSONObject o = Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_USER_LIST_RESPOND);
						o.put("rcList", rcList);
						
						sendProtocol(o);
						
					}
					else if(type.equals(NetworkProtocols.MESSAGE_SEND_REQUEST))
		            {
		                String sender = request.get("sender").toString();
		                ArrayList<String> reciever = (ArrayList<String>) request.get("reciever");
		                String msgTitle = request.get("msgTitle").toString();
		                String msgContent = request.get("msgContent").toString();
		                  
		                for(int i=0; reciever.size()>i ; i++)
		                {
		                	String send_qurey = "insert into �޼��� (�߽���,������,�޼�������,�޼�������,�߽Žð�) values("+"'"+sender+"'"+","+"'"+reciever.get(i).split(",")[0]+"'"+","+"'"+msgTitle+"'"+","+
		                           "'"+msgContent+"'"+","+"now())";
		                    handler.excuteUpdate(send_qurey);
		                    
		                    for(ConnectedClient c : clients)
		                    {
		                    	if(c.getClientID().equals(reciever.get(i).split(",")[0]))
		                    	{
		                    		JSONObject obj = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_MESSAGE_DIALOG);
		                    		obj.put("msg", "���ο� �޼����� �����߽��ϴ�.");
		                    		c.send(obj);
		                    	}
		                    }
						}
		                
						sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_SEND_RESPOND));
		            }
					else if(type.equals(NetworkProtocols.BOARD_MAIN_REQUEST))
					{
						ResultSet rs = handler.excuteQuery("select G.�Խñ۹�ȣ, S.�̸�, G.�Խñ�����, G.�ۼ����� from ����� S, �Խñ� G "
		                         + "where S.�й�=G.�ۼ��� and G.ī�װ���='��������';");
						JSONArray arr = new JSONArray();
						
						String[] keys = {"No","�̸�","�Խñ�����","�ۼ�����"};
						Object[] values = new Object[4];
						
						while(rs.next())
						{
							values[0] = rs.getString("�Խñ۹�ȣ").toString();
							values[1] = rs.getString("�̸�").toString();
							values[2] = rs.getString("�Խñ�����").toString();
							values[3] = rs.getDate("�ۼ�����");
							arr.add(Toolbox.createJSONProtocol(keys, values));
						}
						
						JSONObject res = Toolbox.createJSONProtocol(NetworkProtocols.BOARD_MAIN_RESPOND);
						res.put("board_list", arr);
						sendProtocol(res);
					}
					else if(type.equals(NetworkProtocols.MESSAGE_RECIEVE_LIST_REQUEST))
					{
						String qry = "select M.�޼�����ȣ, S.�̸�, M.�޼�������, M.�߽Žð� from �޼��� M, ����� S where M.������='"+userIdentify+"' and M.�߽���=S.�й�";
						
						ResultSet rs = handler.excuteQuery(qry);
						JSONArray mArr = new JSONArray();
						
						String[] keys = {"No","�߽���","�޼�������","�߽Žð�"};
						
						while(rs.next())
						{
							Object[] values = {
									rs.getString("�޼�����ȣ"),
									rs.getString("�̸�"),
									rs.getString("�޼�������"),
									rs.getDate("�߽Žð�")
							};
							mArr.add(Toolbox.createJSONProtocol(keys, values));
						}
						
						JSONObject res = Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_RECIEVE_LIST_RESPOND);
						res.put("message_list", mArr);
						sendProtocol(res);
					}
					else if(type.equals(NetworkProtocols.MESSAGE_SHOW_MESSAGE_TAP_REQUEST))
					{
						String qry = "select M.�޼�����ȣ, S.�̸�, M.�޼�������, M.�߽Žð� from �޼��� M, ����� S where M.������='"+userIdentify+"' and M.�߽���=S.�й�";
						
						ResultSet rs = handler.excuteQuery(qry);
						JSONArray mArr = new JSONArray();
						
						String[] keys = {"No","�߽���","�޼�������","�߽Žð�"};
							
						while(rs.next())
						{
							Object[] values = {
									rs.getString("�޼�����ȣ"),
									rs.getString("�̸�"),
									rs.getString("�޼�������"),
									rs.getDate("�߽Žð�")
							};
							mArr.add(Toolbox.createJSONProtocol(keys, values));
						}
						
						JSONObject res = Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_SHOW_MESSAGE_TAP_RESPOND);
						res.put("message_list", mArr);
						sendProtocol(res);
					}
					else if(type.equals(NetworkProtocols.MESSAGE_SEND_LIST_REQUEST))
					{
						String qry = "select M.�޼�����ȣ, S.�̸�, M.�޼�������, M.�߽Žð� from �޼��� M, ����� S where M.�߽���='"+userIdentify+"' and M.������=S.�й�";
						
						ResultSet rs = handler.excuteQuery(qry);
						JSONArray mArr = new JSONArray();
						String[] keys = {"No","�߽���","�޼�������","�߽Žð�"};
						
						while(rs.next())
						{
							Object[] values = {
									rs.getString("�޼�����ȣ"),
									rs.getString("�̸�"),
									rs.getString("�޼�������"),
									rs.getDate("�߽Žð�")
							};
							mArr.add(Toolbox.createJSONProtocol(keys, values));
						}
						
						JSONObject res = Toolbox.createJSONProtocol(NetworkProtocols.MESSAGE_SEND_LIST_RESPOND);
						res.put("message_list", mArr);
						sendProtocol(res);
					}
					else if(type.equals(NetworkProtocols.VIDIO_REQUEST))
					{
						byte[] arr = Files.readAllBytes(Paths.get("c:\\movie\\��.mp4"));
						 
						JSONObject j = Toolbox.createJSONProtocol(NetworkProtocols.VIDIO_RESPOND);
						j.put("vdata", arr);
						sendProtocol(j);
					}
					else if(type.equals(NetworkProtocols.MESSAGE_CONTENT_REQUEST))
					{
						int reqNo = (int)request.get("No");
						if(PRINT_LOG)System.out.println("�޼��� ���� ��û��ȣ : "+reqNo);
						JSONObject send_json = new JSONObject();
						String qry="";
						 
						if(request.get("content_type").toString().equals("send"))
						{
                            send_json.put("content_type", "send");
                            qry = "select M.�޼�����ȣ, S.�̸�, M.�޼�������, M.�޼�������, M.�߽Žð� from �޼��� M, ����� S where M.������=S.�й� and M.�޼�����ȣ= "+reqNo;									
						}
						else
						{
							send_json.put("content_type", "recieve");
							qry = "select M.�޼�����ȣ, S.�̸�, M.�޼�������, M.�޼�������, M.�߽Žð� from �޼��� M, ����� S where M.�߽���=S.�й� and M.�޼�����ȣ= "+reqNo;
						}
						
						ResultSet rs = handler.excuteQuery(qry);
						
						while(rs.next())
						{
							String sender_m = rs.getString("�̸�");
                            String msgTitle_m = rs.getString("�޼�������");
                            String msgContent_m = rs.getString("�޼�������");
                            String sendTime_m = rs.getString("�߽Žð�");

                            send_json.put("type",NetworkProtocols.MESSAGE_CONTENT_RESPOND);
                            send_json.put("�߽���", sender_m);
                            send_json.put("�޼�������", msgTitle_m);
                            send_json.put("�޼�������", msgContent_m);
                            send_json.put("�߽Žð�", sendTime_m);
                             
                            sendProtocol(send_json);
						}
					 }
					 else if(type.equals(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_REQUEST))
					 {
						 JSONObject respond = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_RESPOND);
						 String qry = "";
						 if(request.get("viewtype").equals("�޷¸��"))
						 {
							 CalendarDataManager c = new CalendarObject().getDataInstance();
							 qry = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(c.getStartDateOfMonth(), c.getEndDateOfMonth())+" order by �Ͻ� asc;";
						 }
						 else
						 {
							 java.util.Date today = new java.util.Date(System.currentTimeMillis());
							 String todayString = new SimpleDateFormat("yyyy-MM-dd").format(today);
							 qry = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(todayString, todayString)+" order by �Ͻ� asc;";							 
						 }
						 System.out.println(qry);
						 JSONArray sList = new JSONArray();
						 try
						 {
							 ResultSet rs = handler.excuteQuery(qry);
							 String[] keys = {"������ȣ","�����̸�","�Ͻ�","�з�","����"};
							 while(rs.next())
							 {
								 Object[] values = {rs.getInt("������ȣ"),
										 			rs.getString("�����̸�"), 
										 			rs.getTimestamp("�Ͻ�"),
										 			rs.getString("�з�"),
										 			rs.getString("����")};
								 sList.add(Toolbox.createJSONProtocol(keys, values));
							 }
						 }
						 catch(SQLException e)
						 {
							 e.printStackTrace();
						 }
						 
						 respond.put("todays", sList);
						 sendProtocol(respond);
					 }
					 else if(type.equals(NetworkProtocols.MODIFY_SCHEDULE_REQUEST))
					 {
						 String qry = "update ���� set �����̸�='"+request.get("�����̸�")+"', "
						 		+ "�Ͻ�=date_format('"+request.get("�Ͻ�")+"','%Y-%c-%d %H:%i:%s'), "
						 		+ "�з�='"+request.get("�з�")+"', "
						 		+ "����='"+request.get("����")+"'"
						 		+ "where ������ȣ="+request.get("������ȣ");
						 handler.excuteUpdate(qry);
						 
						 JSONObject respond = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_RESPOND);
						 String startDate = request.get("������").toString();
						 String endDate = request.get("������").toString();
						 
						 java.util.Date today = new java.util.Date(System.currentTimeMillis());
						 String todayString = new SimpleDateFormat("yyyy-MM-dd").format(today);
						 
						 if(startDate.length()==0)
						 {
							 startDate = todayString;
						 }
						 
						 if(endDate.length()==0)
						 {
							 endDate = todayString;							 
						 }
						 
						 String qry2 = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(startDate, endDate)+" order by �Ͻ� asc;";
						 
						 if(request.get("viewtype").equals("�޷¸��"))
						 {
							 CalendarDataManager c = new CalendarObject().getDataInstance();
							 try
							 {
								 c.getCalander().setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(request.get("�Ͻ�").toString()));
							 }
							 catch(ParseException e)
							 {
								 e.printStackTrace();
							 }
							 qry2 = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(c.getStartDateOfMonth(), c.getEndDateOfMonth())+" order by �Ͻ� asc;";
						 }
						 System.out.println(qry);
						 JSONArray sList = new JSONArray();
						 try
						 {
							 ResultSet rs = handler.excuteQuery(qry2);
							 String[] keys = {"������ȣ","�����̸�","�Ͻ�","�з�","����"};
							 while(rs.next())
							 {
								 Object[] values = {rs.getInt("������ȣ"),
										 			rs.getString("�����̸�"), 
										 			rs.getTimestamp("�Ͻ�"),
										 			rs.getString("�з�"),
										 			rs.getString("����")};
								 sList.add(Toolbox.createJSONProtocol(keys, values));
							 }
						 }
						 catch(SQLException e)
						 {
							 e.printStackTrace();
						 }
						 
						 respond.put("todays", sList);
						 sendProtocol(respond);
						 
					 }
					 else if(type.equals(NetworkProtocols.ADD_SCHEDULE_REQUEST))
					 {
						 String qry = "insert into ����(�����̸�,�Ͻ�,�з�,����) values('"+request.get("����")+"', date_format('"+request.get("�Ͻ�")+"','%Y-%c-%d %H:%i:%s'), '"+request.get("�з�")+"', '"+request.get("����")+"');";
						 
						 handler.excuteUpdate(qry);
						 
						 JSONObject respond = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_RESPOND);
						 String startDate = request.get("������").toString();
						 String endDate = request.get("������").toString();
						 
						 java.util.Date today = new java.util.Date(System.currentTimeMillis());
						 String todayString = new SimpleDateFormat("yyyy-MM-dd").format(today);
						 
						 if(startDate.length()==0)
						 {
							 startDate = todayString;
						 }
						 
						 if(endDate.length()==0)
						 {
							 endDate = todayString;							 
						 }
						 
						 String qry2 = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(startDate, endDate)+" order by �Ͻ� asc;";
						 
						 if(request.get("viewtype").equals("�޷¸��"))
						 {
							 CalendarDataManager c = new CalendarObject().getDataInstance();
							 try
							 {
								 c.getCalander().setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(request.get("�Ͻ�").toString()));
							 }
							 catch(ParseException e)
							 {
								 e.printStackTrace();
							 }
							 qry2 = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(c.getStartDateOfMonth(), c.getEndDateOfMonth())+" order by �Ͻ� asc;";
						 }
						 
						 JSONArray sList = new JSONArray();
						 try
						 {
							 ResultSet rs = handler.excuteQuery(qry2);
							 String[] keys = {"������ȣ","�����̸�","�Ͻ�","�з�","����"};
							 while(rs.next())
							 {
								 Object[] values = {rs.getInt("������ȣ"),
										 			rs.getString("�����̸�"), 
										 			rs.getTimestamp("�Ͻ�"),
										 			rs.getString("�з�"),
										 			rs.getString("����")};
								 sList.add(Toolbox.createJSONProtocol(keys, values));
							 }
						 }
						 catch(SQLException e)
						 {
							 e.printStackTrace();
						 }
						 
						 respond.put("todays", sList);
						 sendProtocol(respond);
					 }
					 else if(type.equals(NetworkProtocols.DELETE_SCHEDULE_REQUEST))
					 {
						 String startDate = request.get("������").toString();
						 String endDate = request.get("������").toString();
						 String qry = "";

						 
						 qry = "delete from ���� where ������ȣ = "+request.get("������ȣ");
						 handler.excuteUpdate(qry);
						 
						 JSONObject respond = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_RESPOND);
						 
						 
						 java.util.Date today = new java.util.Date(System.currentTimeMillis());
						 String todayString = new SimpleDateFormat("yyyy-MM-dd").format(today);
						 
						 if(startDate.length()==0)
						 {
							 startDate = todayString;
						 }
						 
						 if(endDate.length()==0)
						 {
							 endDate = todayString;							 
						 }
						 
						 String qry2 = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(startDate, endDate)+" order by �Ͻ� asc;";
						 
						 if(request.get("viewtype").equals("�޷¸��"))
						 {
							 CalendarDataManager c = new CalendarObject().getDataInstance();
							 try
							 {
								 c.getCalander().setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(request.get("�Ͻ�").toString()));
							 }
							 catch(ParseException e)
							 {
								 e.printStackTrace();
							 }
							 qry2 = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(c.getStartDateOfMonth(), c.getEndDateOfMonth())+" order by �Ͻ� asc;";
						 }
						 
						 JSONArray sList = new JSONArray();
						 try
						 {
							 ResultSet rs = handler.excuteQuery(qry2);
							 String[] keys = {"������ȣ","�����̸�","�Ͻ�","�з�","����"};
							 while(rs.next())
							 {
								 Object[] values = {rs.getInt("������ȣ"),
										 			rs.getString("�����̸�"), 
										 			rs.getTimestamp("�Ͻ�"),
										 			rs.getString("�з�"),
										 			rs.getString("����")};
								 sList.add(Toolbox.createJSONProtocol(keys, values));
							 }
						 }
						 catch(SQLException e)
						 {
							 e.printStackTrace();
						 }
						 
						 respond.put("todays", sList);
						 sendProtocol(respond);
					 }
					 else if(type.equals(NetworkProtocols.SCHEDULE_PROFESSIONAL_SEARCH_REQUEST))
					 {
						 JSONObject respond = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_SCHEDULE_MANAGER_TAB_RESPOND);
						 String sp = request.get("�з�").toString();
						 String qry;
						 if(sp.equals("��ü"))
						 {
							 qry = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(request.get("������").toString(), request.get("������").toString())+" order by �Ͻ� asc;";							 
						 }
						 else
						 {
							 qry = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(request.get("������").toString(), request.get("������").toString())+"and �з�='"+sp+"' order by �Ͻ� asc;";
						 }
						 
						 JSONArray sList = new JSONArray();
						 try
						 {
							 ResultSet rs = handler.excuteQuery(qry);
							 String[] keys = {"������ȣ","�����̸�","�Ͻ�","�з�","����"};
							 while(rs.next())
							 {
								 Object[] values = {rs.getInt("������ȣ"),
										 			rs.getString("�����̸�"), 
										 			rs.getTimestamp("�Ͻ�"),
										 			rs.getString("�з�"),
										 			rs.getString("����")};
								 sList.add(Toolbox.createJSONProtocol(keys, values));
							 }
						 }
						 catch(SQLException e)
						 {
							 e.printStackTrace();
						 }
						 
						 respond.put("todays", sList);
						 sendProtocol(respond);
						 
					 }
					 else if(type.equals(NetworkProtocols.MONTHLY_SCHEDULE_VIEW_REQUEST))
					 {
						 String qry = "select * from ���� where "+Toolbox.getWhereStringBetweenDate(request.get("startDate").toString(), request.get("endDate").toString())+" order by �Ͻ� asc;";
						 System.out.println(qry);
						 JSONObject respond = Toolbox.createJSONProtocol(NetworkProtocols.MONTHLY_SCHEDULE_VIEW_RESPOND);
						 
						 JSONArray sList = new JSONArray();
						 try
						 {
							 ResultSet rs = handler.excuteQuery(qry);
							 String[] keys = {"������ȣ","�����̸�","�Ͻ�","�з�","����"};
							 while(rs.next())
							 {
								 Object[] values = {rs.getInt("������ȣ"),
										 			rs.getString("�����̸�"), 
										 			rs.getTimestamp("�Ͻ�"),
										 			rs.getString("�з�"),
										 			rs.getString("����")};
								 sList.add(Toolbox.createJSONProtocol(keys, values));
							 }
						 }
						 catch(SQLException e)
						 {
							 e.printStackTrace();
						 }
						 
						 respond.put("todays", sList);
						 respond.put("viewableDate", request.get("startDate").toString());
						 sendProtocol(respond);
					 }
					 else if(type.equals(NetworkProtocols.SHOW_USER_INFO_TAP_REQUEST))
	                {
	                   String qry = "select S.�й�,S.�̸� from ����� S";
	                   
	                   ResultSet rs = handler.excuteQuery(qry);
	                   
	                   JSONArray mArr = new JSONArray();
	                     
	                   String[] keys = {"�й�","�̸�"};
	                      while(rs.next())
	                     {
	                        Object[] values = {
	                              rs.getString("�й�"),
	                              rs.getString("�̸�")
	                        };
	                        mArr.add(Toolbox.createJSONProtocol(keys, values));
	                     }
	                     // ���ο� ���̽��� �������� ����
	                     JSONObject res = Toolbox.createJSONProtocol(NetworkProtocols.SHOW_USER_INFO_TAP_RESPOND);
	                     // ���̽� ��� ���� 
	                     res.put("user_list", mArr);
	                     sendProtocol(res);
	                }
	                else if(type.equals(NetworkProtocols.USER_CONTENT_REQUEST))
	                {
	                   String name = request.get("�̸�").toString();
	                   String num = request.get("�й�").toString();
	                   
	                   String qry = "select * from ����� where �̸�='"+name+"'"+"and �й� = '"+num+"'";
	                   ResultSet rs = handler.excuteQuery(qry);
	                   
	                   JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.USER_CONTENT_RESPOND);
	                   
	                   while(rs.next())
	                   {
	                      String studnet_name = rs.getString("�̸�");
	                      String student_num = rs.getString("�й�");
	                      
	                      json.put("�й�", student_num);
	                      json.put("�̸�", studnet_name);
	                   }
	                   sendProtocol(json);
	                }
	                else if(type.equals(NetworkProtocols.WEABAK_INFO_TAP_REQUEST))
	                {
	                   
	                   String category = request.get("category").toString();
	               
	                   String qry;
	                   ResultSet rs = null;
	                   
	                  
	                   if(category == "main")
	                   {
	                      qry = "select W.�ܹڹ�ȣ,S.�й� ,S.�̸�, W.���� ,W.��û���� ,W.�ܹ����� ,W.������ ,W.���ο��� from �ܹ� W, ����� S where S.�й� = W.��û�� and W.���ο��� =0 ";
	                      rs = handler.excuteQuery(qry);
	                   }
	                   else if(category.equals("�����"))
	                   {
	                      qry = "select W.�ܹڹ�ȣ,S.�й� ,S.�̸�, W.���� ,W.��û���� ,W.�ܹ����� ,W.������ ,W.���ο��� from �ܹ� W, ����� S where S.�й� = W.��û�� and W.���ο��� =0 ";
	                      rs = handler.excuteQuery(qry);
	                   }
	                   else if(category.equals("���οܹ�"))
	                   {
	                      qry = "select W.�ܹڹ�ȣ,S.�й� ,S.�̸�, W.���� ,W.��û���� ,W.�ܹ����� ,W.������ ,W.���ο��� from �ܹ� W, ����� S where S.�й� = W.��û�� and W.���ο��� =1 ";
	                      rs = handler.excuteQuery(qry);
	                   }
	                   else
	                   {
	                      qry = "select W.�ܹڹ�ȣ,S.�й� ,S.�̸�, W.���� ,W.��û���� ,W.�ܹ����� ,W.������ ,W.���ο��� from �ܹ� W, ����� S where S.�й� = W.��û�� and W.���ο��� =2 ";
	                      rs = handler.excuteQuery(qry);
	                   }
	                   
	                   JSONArray jarr = new JSONArray();
	                   String keys[] = {"�ܹڹ�ȣ","�̸�","�й�","����","��û����","�ܹ�����","������","���ο���"};
	                   while(rs.next())
	                   {
	                      Object[] values ={
	                            rs.getString("�ܹڹ�ȣ"),
	                            rs.getString("�̸�"),
	                            rs.getString("�й�"),
	                            rs.getString("����"),
	                            rs.getString("��û����"),
	                            rs.getString("�ܹ�����"),
	                            rs.getString("������"),
	                            rs.getString("���ο���")
	                      };
	                      
	                      jarr.add(Toolbox.createJSONProtocol(keys, values)); 
	                   }
	                   JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_INFO_TAP_RESPOND);
	                   json.put("weabak_list", jarr);
	                   sendProtocol(json);
	                }
	                else if(type.equals(NetworkProtocols.PLUS_MINUS_TAP_REQUEST))
	                {
	                   String qry = "select * from �����";
	                   String plus = null;
	                   String minus = null;
	                   int sum;
	                   ResultSet rs = handler.excuteQuery(qry);
	                   
	                   JSONArray jarr = new JSONArray();
	                   String keys[] = {"�й�","�̸�","����","����","�հ�"};
	                   while(rs.next())
	                   {
	                      String num = rs.getString("�й�");
	                      System.out.println("�й� ��� : "+num);
	                      
	                      String plusqry = "select sum(����) from ������ο���� where �й� ="+"'"+num+"'"+"and "+"�����Ÿ�� = '����'";
	                      String minusqry = "select sum(����) from ������ο���� where �й� ="+"'"+num+"'"+"and "+"�����Ÿ�� = '����'";
	                      
	                      ResultSet rs1 = handler.excuteQuery(plusqry);
	                      ResultSet rs2 = handler.excuteQuery(minusqry);
	                      
	                      while(rs1.next())
	                      {
	                         plus = rs1.getString("sum(����)");
	                      }
	                      while(rs2.next())
	                      {
	                         minus = rs2.getString("sum(����)");
	                      }
	                      
	                      if(plus == null)
	                      {
	                         plus = "0";
	                      }
	                      if(minus == null)
	                      {
	                         minus = "0";
	                      }
	                      
	                      System.out.println("���� : "+plus);
	                      System.out.println("���� : "+minus);
	                      
	                      
	                      sum = Integer.parseInt(plus)+Integer.parseInt(minus);
	                      
	                      Object[] values ={
	                            rs.getString("�й�"),
	                            rs.getString("�̸�"),
	                            plus,
	                            minus,
	                            sum
	                      };
	                      
	                      jarr.add(Toolbox.createJSONProtocol(keys, values)); 
	                   }
	                   
	                   JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.PLUS_MINUS_TAP_RESPOND);
	                   json.put("plusminus_list", jarr);
	                   sendProtocol(json);
	                }
	                //�߰�
	                else if(type.equals(NetworkProtocols.WEABAK_CONTENT_REQUEST))
	                {
	                   
	                   int No = (int) request.get("No");
	                   String qry = "select * from �ܹ� where �ܹڹ�ȣ = "+"'"+No+"'";
	                   String name = request.get("�̸�").toString();
	                   
	                      ResultSet rs = handler.excuteQuery(qry);
	                      
	                      JSONObject arr = null;
	                      
	                      String keys[] = {"�ܹڹ�ȣ","�й�","����","��û����","�ܹ�����","������","���ο���"};
	                      while(rs.next())
	                      {
	                         Object values[] = {rs.getInt("�ܹڹ�ȣ"),rs.getString("��û��"),rs.getString("����"),rs.getDate("��û����"),rs.getDate("�ܹ�����"),rs.getString("������"),rs.getInt("���ο���")};
	                         arr = Toolbox.createJSONProtocol(keys, values); 
	                      }
	                      arr.put("�̸�", name);
	                      
	                      JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.WEQBAK_CONTENT_RESPOND);
	                      json.put("weabak_content_list", arr);
	                      sendProtocol(json);
	                  
	                }
	                else if(type.equals(NetworkProtocols.PLUS_MINUS_TAP_INFO_REQUEST))
	                {
	                   String qry = "select P.������ο���ȣ,P.��¥,S.�й� ,S.�̸�, P.���� ,P.���� ,P.�����Ÿ��  from ������ο���� P, ����� S where S.�й� = P.�й�";
	                   
	                   ResultSet rs = handler.excuteQuery(qry);
	                   JSONArray jarray = new JSONArray();
	                   
	                   String keys[] = {"No","��¥","�й�","�̸�","����","����","�����Ÿ��"};
	                   while(rs.next())
	                   {
	                      Object values[] = {
	                            rs.getInt("������ο���ȣ"),
	                            rs.getDate("��¥"),
	                            rs.getString("�й�"),
	                            rs.getString("�̸�"),
	                            rs.getString("����"),
	                            rs.getInt("����"),
	                            rs.getString("�����Ÿ��")       
	                      };
	                      jarray.add(Toolbox.createJSONProtocol(keys, values));
	                   }
	                   JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.PLUS_MINUS_TAP_INFO_RESPOND);
	                   json.put("plus_minus_check_list", jarray);
	                   
	                   sendProtocol(json);
	                   
	                }
					 else if(type.equals(NetworkProtocols.MY_OVERNIGHT_LIST_REQUEST))
					 {
						 JSONObject respond = Toolbox.createJSONProtocol(NetworkProtocols.MY_OVERNIGHT_LIST_RESPOND);
						 
						 String qry = "select * from �ܹ� where ��û��='"+request.get("uID").toString()+"' order by ��û���� asc;";
						 
						 try
						 {
							 ResultSet rs = handler.excuteQuery(qry);

							 String[] keys = {"��û����", "�ܹ�����", "������", "����", "���ο���"};
							 JSONArray jArr = new JSONArray();
							 while(rs.next())
							 {
								 Object[] values = {rs.getTimestamp("��û����"), rs.getDate("�ܹ�����"), rs.getString("������"), rs.getString("����"), rs.getInt("���ο���")};
								 jArr.add(Toolbox.createJSONProtocol(keys, values));
							 }
							 respond.put("data", jArr);
							 sendProtocol(respond);
						 }
						 catch(SQLException e)
						 {
							 e.printStackTrace();
						 }
					 }
					 else if(type.equals(NetworkProtocols.ENROLL_OVERNIGHT_REQUEST))
					 {
						 JSONObject obj = Toolbox.createJSONProtocol(NetworkProtocols.ENROLL_OVERNIGHT_RESPOND);
						 // �˻� ���� �� �ܹ� ��û ���ϴ¾ָ� result�� �׿��°�����
						 
						 String qry = "insert into �ܹ�(��û��,����,��û����,�ܹ�����,������) values('"+userIdentify+"','"+request.get("����")+"',now(),'"+request.get("�ܹ�����")+"','"+request.get("������")+"');";
						 
						 handler.excuteUpdate(qry);
						 
						 obj.put("result", "OK");
						 
						 sendProtocol(obj);
						 
						 for(ConnectedClient c : clients)
						 {
							 if(c.getClientGrade().equals("������"))
							 {
				                   String category = "�����";
					               
				                   String qry2;
				                   ResultSet rs = null;
				                   
				                  
				                   qry2 = "select W.�ܹڹ�ȣ,S.�й� ,S.�̸�, W.���� ,W.��û���� ,W.�ܹ����� ,W.������ ,W.���ο��� from �ܹ� W, ����� S where S.�й� = W.��û�� and W.���ο��� = 0 ";
				                   rs = handler.excuteQuery(qry2);
				                   
				                   JSONArray jarr = new JSONArray();
				                   String keys[] = {"�ܹڹ�ȣ","�̸�","�й�","����","��û����","�ܹ�����","������","���ο���"};
				                   while(rs.next())
				                   {
				                      Object[] values ={
				                            rs.getString("�ܹڹ�ȣ"),
				                            rs.getString("�̸�"),
				                            rs.getString("�й�"),
				                            rs.getString("����"),
				                            rs.getString("��û����"),
				                            rs.getString("�ܹ�����"),
				                            rs.getString("������"),
				                            rs.getString("���ο���")
				                      };
				                      
				                      jarr.add(Toolbox.createJSONProtocol(keys, values)); 
				                   }
				                   JSONObject json = Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_INFO_TAP_RESPOND);
				                   json.put("weabak_list", jarr);
				                   sendProtocol(json);
							 }
						 }
					 }
					 else if(type.equals(NetworkProtocols.WEABAK_PROCESS_REQUEST))
					 {
						 
						 String action = request.get("action").toString();
						 String reqNo = request.get("reqNo").toString();
						 
						 String qry = "update �ܹ� set ���ο��� = "+action+" where �ܹڹ�ȣ = "+reqNo+";";
						 
						 handler.excuteUpdate(qry);
						 
						 sendProtocol(Toolbox.createJSONProtocol(NetworkProtocols.WEABAK_PROCESS_RESPOND));
					 }
					 else if(type.equals(NetworkProtocols.STUDENT_RECIEVE_MESSAGE_REQUEST))
					 {
						 String qry = "select S.�̸�, M.�޼�������, M.�߽Žð�, M.�޼������� from �޼��� M, ����� S where M.������='"+userIdentify+"' and M.�߽���=S.�й� order by �߽Žð� asc";
							
						ResultSet rs = handler.excuteQuery(qry);
						JSONArray mArr = new JSONArray();
						
						String[] keys = {"�߽���","�޼�������","�߽Žð�", "�޼�������"};
						
						while(rs.next())
						{
							Object[] values = {
									rs.getString("�̸�"),
									rs.getString("�޼�������"),
									rs.getDate("�߽Žð�"),
									rs.getString("�޼�������")
							};
							mArr.add(Toolbox.createJSONProtocol(keys, values));
						}
						
						JSONObject o = Toolbox.createJSONProtocol(NetworkProtocols.STUDENT_RECIEVE_MESSAGE_RESPOND);
						o.put("data", mArr);
						sendProtocol(o);
						
					 }
					 else if(type.equals(NetworkProtocols.STUDENT_SEND_MESSAGE_REQUEST))
					 {
						 String qry = "select S.�̸�, M.�޼�������, M.�߽Žð�, M.�޼������� from �޼��� M, ����� S where M.�߽���='"+userIdentify+"' and M.������=S.�й� order by �߽Žð� asc";
							
						ResultSet rs = handler.excuteQuery(qry);
						JSONArray mArr = new JSONArray();
						
						String[] keys = {"������","�޼�������","�߽Žð�", "�޼�������"};
						
						while(rs.next())
						{
							Object[] values = {
									rs.getString("�̸�"),
									rs.getString("�޼�������"),
									rs.getDate("�߽Žð�"),
									rs.getString("�޼�������")
							};
							mArr.add(Toolbox.createJSONProtocol(keys, values));
						}
						
						JSONObject o = Toolbox.createJSONProtocol(NetworkProtocols.STUDENT_SEND_MESSAGE_RESPOND);
						o.put("data", mArr);
						sendProtocol(o);
						
					 }
					 else
					 {
						 if(PRINT_LOG)System.out.println("\t\t\t�߸��� ��û");
					 }
				}
			}
			catch (IOException|SQLException e)
			{
				/* ����ڰ� �����Ѱ��� */
				if(PRINT_LOG)System.out.println("\t\t[Connector-"+userName+"] ����� ���� ����, ������ ����");
			}
			
		}
		
		public void sendProtocol(JSONObject protocol) throws IOException
		{
			toClient.writeObject(protocol);
			toClient.flush();
		}
		
	}

	class Controller extends Thread
	{
		@Override
		public void run() {			
			@SuppressWarnings("resource")
			java.util.Scanner sc = new java.util.Scanner(System.in);
			String command = "";
			
			System.out.println("DIMS Server Controller");
			System.out.println("/? or help - show command list, start server - Run server, exit - exit program");
			while(true)
			{
				System.out.print("Input >> ");
				command = sc.nextLine();
				
				if(command.equals("exit"))
				{
					System.exit(0);
				}
				else if(command.equals("/?")||command.equals("help"))
				{
					System.out.println("print help list");
				}
				else if(command.equals("set PRINT_LOG = true"))
				{
					PRINT_LOG = true;
				}
				else if(command.equals("set PRINT_LOG = false"))
				{
					PRINT_LOG = false;
				}
				else if(command.equals("show clients"))
				{
					System.out.println("Current connected clients");
					System.out.println("Clients count : "+ clients.size());
					for(ConnectedClient c : clients)
					{
						System.out.println("ClientID   : "+c.getClientID());
						System.out.println("ClientName : "+c.getClientName());
					}
				}
				else if(command.equals("start server"))
				{
					serverOpen();
				}
				else
				{
					System.out.println("Invalid Command");
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		
		DIMS_Server s = new DIMS_Server();
		s.serverOpen();
	}
	
}