package tools;

public class NetworkProtocols {
		
	public static final String LOGIN_REQUEST = "logreq";
	public static final String LOGIN_ACCEPT = "logaccept";
	public static final String LOGIN_DENY = "logdeny";
	
	public static final String ID_DUP_CHECK_REQUEST = "iddupcheckreq";
	public static final String ID_DUP_RESPOND_OK = "dupok";
	public static final String ID_DUP_RESPOND_DENY = "dupdeny";
	
	public static final String EXIT_REQUEST = "exitreq";
	public static final String EXIT_RESPOND = "exitres";
	
	public static final String INVALID_REQUEST_ERROR = "invalidreqerr";
	
	public static final String WINDOW_INIT_PROPERTY = "wininitproperty";
	
	public static final String RECIEVE_READY = "isready?";
	public static final String RECIEVE_READY_OK = "readyok";
	
	public static final String PLZ_REQUEST = "gogo";
	
	public static final String VIDIO_REQUEST = "vreq";
	public static final String VIDIO_RESPOND = "vres";
	
	// �޼����� ����
	public static final String MESSAGE_SHOW_MESSAGE_TAP_REQUEST = "msmtreq";
	public static final String MESSAGE_SHOW_MESSAGE_TAP_RESPOND = "msmtres";
	
	// ���� �޼����� ��û
	public static final String MESSAGE_RECIEVE_LIST_REQUEST = "mrlreq";
	public static final String MESSAGE_RECIEVE_LIST_RESPOND = "mrlres";
	
	// ���� �޼����� ��û
	public static final String MESSAGE_SEND_LIST_REQUEST = "mslreq";
	public static final String MESSAGE_SEND_LIST_RESPOND = "mslres";
	
	// ����� ��� ��û
	public static final String MESSAGE_USER_LIST_REQUEST = "mulreq";
	public static final String MESSAGE_USER_LIST_RESPOND = "mulres";
	
	// �޼��� ������ ��û
	public static final String MESSAGE_SEND_REQUEST = "msreq";
	public static final String MESSAGE_SEND_RESPOND = "msacc";
	
	// �޼����� ���� Ŭ����
	public static final String MESSAGE_CONTENT_REQUEST = "mcreq";
	public static final String MESSAGE_CONTENT_RESPOND = "mcres";
	
	// �޼��� �˾� â ����
	public static final String SHOW_MESSAGE_DIALOG = "smdlg";
	
	// �Խ��� �� Ŭ����
	public static final String BOARD_MAIN_REQUEST = "bmreq";
	// '��������'ī�װ����� �Խñ۵��� Ŭ���̾�Ʈ�� ����
	public static final String BOARD_MAIN_RESPOND = "bmres";
		
		// �Խñ� �ۼ�ȭ�鿡�� ���ۼ� Ŭ����
		public static final String ENROLL_BOARD_REQUEST = "ebreq";
		// �Խñ��� ��� ����ϰ� Ŭ���̾�Ʈ���� �˸�
		public static final String ENROLL_BOARD_RESPOND = "ebrespond";
		// �Խñ� ��� ���� ��
		public static final String ENROLL_BOARD_ERROR = "eberr";
		
		// �� ī�װ��� Ŭ����
		public static final String BOARD_LIST_REQUEST = "blistreq";
		// �� ī�װ����� �ִ� �Խñ۸� Ŭ���̾�Ʈ�� ����
		public static final String BOARD_LIST_RESPOND = "blistres";
	
		// �Խñ� ���� Ŭ����
		public static final String BOARD_CONTENT_REQUEST = "bcoreq";
		// �ش� �Խñ��� ������ Ŭ���̾�Ʈ�� ����
		public static final String BOARD_CONTENT_RESPOND = "bcores";
		
		// �Խñ� �˻� ��
		public static final String BOARD_SEARCH_REQUEST ="bsreq";
		// �˻���� ����
		public static final String BOARD_SEARCH_RESPOND ="bsres";
		// �˻���� ������
		public static final String BOARD_NO_SEARCH_RESULT = "nsres";

		// �Ż�������ȸ �� Ŭ�� ��
		public static final String SHOW_USER_INFO_TAB_REQUEST = "suitreq";
		public static final String SHOW_USER_INFO_TAB_RESPOND = "suitres";
		
		//�������� �� Ŭ����
		public static final String SHOW_SCHEDULE_MANAGER_TAB_REQUEST = "ssmtreq";
		public static final String SHOW_SCHEDULE_MANAGER_TAB_RESPOND = "ssmtres";
		
		//���� ���� ��û ��
		public static final String MODIFY_SCHEDULE_REQUEST = "msreq1";
		
		//���� �߰� ��û ��
		public static final String ADD_SCHEDULE_REQUEST = "asreq";
		
		//���� ���� ��û ��
		public static final String DELETE_SCHEDULE_REQUEST = "dsreq";
		
		//���� �˻� ��û ��
		public static final String SCHEDULE_PROFESSIONAL_SEARCH_REQUEST = "spsreq";
		
		//���� �޷¸��� ��ȸ��
		public static final String MONTHLY_SCHEDULE_VIEW_REQUEST = "msvreq";
		public static final String MONTHLY_SCHEDULE_VIEW_RESPOND = "msvres";
		
		//�Ż� ���� ��ȸ �� Ŭ����
	    public static final String SHOW_USER_INFO_TAP_REQUEST ="suitreq";
	    public static final String SHOW_USER_INFO_TAP_RESPOND ="suitres";
		
		//�Ż����� ��ȸ ���� ����Ŭ����
	    public static final String USER_CONTENT_REQUEST = "uccreq";
	    public static final String USER_CONTENT_RESPOND = "ucres"; 
	      
	    //�ܹ� ��ȸ ���� �� Ŭ����
	    public static final String WEABAK_INFO_TAP_REQUEST = "witreq";
	    public static final String WEABAK_INFO_TAP_RESPOND = "witres";
	      
	    //����� �ο� ���� �� Ŭ����
	    public static final String PLUS_MINUS_TAP_REQUEST = "pmtreq";
	    public static final String PLUS_MINUS_TAP_RESPOND = "pmtres";
	      
	    //����� ��ȸ ���� �� Ŭ����
	    public static final String PLUS_MINUS_TAP_INFO_REQUEST = "pmtireq";
	    public static final String PLUS_MINUS_TAP_INFO_RESPOND = "pmtires";
	     
	    //�ܹ� ��ȸ ���� ���� Ŭ����
	    public static final String WEABAK_CONTENT_REQUEST = "wcreq";
	    public static final String WEQBAK_CONTENT_RESPOND = "wcres";
		
	    //�ܹ� ���� 
	    public static final String WEABAK_PROCESS_REQUEST = "wbpreq";
	    public static final String WEABAK_PROCESS_RESPOND = "wbpres";
	    
	    /*-----------------------------------------------------------------*/
		
		// �л�-�ܹ� ���� ����Ʈ ��û
		public static final String MY_OVERNIGHT_LIST_REQUEST = "myolreq";
		public static final String MY_OVERNIGHT_LIST_RESPOND = "myolres";		
		
		// �л�-�ܹ� ��û ��û
		public static final String ENROLL_OVERNIGHT_REQUEST = "eoreq";
		public static final String ENROLL_OVERNIGHT_RESPOND = "eores";
		
		// �л� - ���� �޼��� ����Ʈ ��û
		public static final String STUDENT_RECIEVE_MESSAGE_REQUEST = "srmreq";
		public static final String STUDENT_RECIEVE_MESSAGE_RESPOND = "srmres";
		
		// �л� - ���� �޼��� ����Ʈ ��û
		public static final String STUDENT_SEND_MESSAGE_REQUEST = "ssmreq";
		public static final String STUDENT_SEND_MESSAGE_RESPOND = "ssmres";
		
		// �л� - �޼��� ������ ���ο��
		
		
}