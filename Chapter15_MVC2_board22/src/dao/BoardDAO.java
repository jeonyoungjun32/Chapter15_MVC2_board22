//DB로 SQL구문을 전송하는 클래스
package dao;

//static:모든 메서드들 미리 메모리에 올림
import static db.Jdbcutil.close;
import static db.Jdbcutil.commit;
import static db.Jdbcutil.rollback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import vo.Boardbean;

public class BoardDAO {
	Connection con=null;//멤버변수(전역변수) : 전체 메서드에서 사용 가능
	PreparedStatement pstmt=null;
	ResultSet rs = null;
	/* 싱글톤 패턴 : BoardDAO객체 단 1개만 생성
	 * 이유? 외부 클래스에서 "처음 생성된 BoardDAO객체를 공유해서 사용하도록 하기 위해"
	 */
	
	//싱글톤-1.1
	private BoardDAO() {}
	
	//싱클톤-1.2
	private static BoardDAO boardDAO;
	//static이유? 객체를 생성하기 전에 이미 메모리에 올라간 getInstance()메서드를 통해서만 BoardDAO객체를 만들도록 하기 위해
	public static BoardDAO getInstance() {
		if(boardDAO == null) {//객체가 없으면
			boardDAO = new BoardDAO();//객체 생성
		}
		
		return boardDAO;//기존 객체의 주소 리턴
	}
	
	public void setConnection(Connection con) {//Connection객체를 받아 DB 연결
		this.con=con;
	}
	
	//1.글 등록
	public int insertArticle(Boardbean article) {
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
		int num =0;
		String sql="";
		int insertCount=0;
		try {
			//pstmt = con.prepareStatement("select max(board_num) from board");
			pstmt = con.prepareStatement("select IFNULL(max(board_num),0)+1 from board");
			rs= pstmt.executeQuery();
			
			//if(rs.next()) num = rs.getInt(1)+1; 
			//else num=1;//null이면 1
			if(rs.next()) num=rs.getInt(1);
			
			sql = "insert into board values(?,?,?,?,?,?,?,?,?,?,now())";//now()=오라클 sysdate
			pstmt= con.prepareStatement(sql);
			
			pstmt.setInt(1, num);
			pstmt.setString(2, article.getBoard_name());//글쓴이
			pstmt.setString(3, article.getBoard_pass());//비밀번호
			pstmt.setString(4, article.getBoard_subject());//제목
			pstmt.setString(5, article.getBoard_content());//내용
			pstmt.setString(6, article.getBoard_file());//첨부 파일
			
			//답변글 등록할 때 '원글과 답변글'을 '같은그룹'으로 묶기우해 사용함(그룹번호가 같으면 같은번호)
			pstmt.setInt(7, num);
			pstmt.setInt(8, 0);//얼마만큼 안쪽으로 들어가 글이 시작될 것인지를 결정해 주는 값(0으로 초기화. 0은 원글)
			pstmt.setInt(9, 0);//원글에서 답변글이 몇 번째 아래에 놓일 것인지 위치를 결정해 주는 값(0으로 초기화)
			
			pstmt.setInt(10, 0);//조회수(0으로 초기화)
			//11 : now()오늘날짜
			
			insertCount = pstmt.executeUpdate();//업데이트가 성공하면 1을 리턴받음
		} catch (Exception e) {
			System.out.println("insertArticle 에러 : "+e);//e:예외종류+예외메세지
			e.printStackTrace();
		} finally {
			close(rs);
			close(pstmt);
		}
		return insertCount;
	}
	
	/*2.게시판 전체 글의 개수*/
	public int selectListCount() {
		int ListCount=0;
		try {
			pstmt = con.prepareStatement("select count(*) from board");
			rs= pstmt.executeQuery();
			
			if(rs.next()) {
				ListCount=rs.getInt(1);
			}
			
		} catch (Exception e) {
			System.out.println("getListCount 에러 : "+e);//e:예외종류+예외메세지
			e.printStackTrace();
		} finally {
			close(pstmt);
			close(rs);
		}
		
		return ListCount;
	}
	
	//3.해당 페이지에 출력될 글 목록을 DB에서 조회해서 ArrayList<Boardbean>객체 반환
	public ArrayList<Boardbean> selectArticleList(int page, int limit) {
		ArrayList<Boardbean> articleList  = new ArrayList<Boardbean>();
		
		/* board_re_ref : 같은 수는 같은 그룹
		 * 					(원글 번호가 3이면 답변글도 모두 3)
		 * board_re_sql : 원글에서 답변글이 몇 번째 아래에 놓일 것인지 위치를 결정해 주는 값
		 * 
		 * limit ?,10 mysql에만 있음
		 */
		
		//limit 4,10 :5행부터 10개
		
		String sql ="select * from board order by board_re_ref desc, board_re_seq asc limit ?,10";
		
		/*
		 * startrow변수에 해당 페이지에서 출력되어야 하는 시작 레코드의 index번호를 구하여 저장
		 * (예)아래 페이지 번호 중 2를 클릭하면 page가 2가 되어 (2-1)*10=10
		 */
		int startrow = (page-1)*10;//10이지만 읽기 시작하는 row번호는 11이 됨
		try {
			pstmt = con.prepareStatement(sql);
			
			pstmt.setInt(1, startrow);//11부터 10개의 레코드 조회(답변글 포함해서)
			rs = pstmt.executeQuery();
			
			Boardbean boardbean = null;
			
			while (rs.next()) {
				boardbean = new Boardbean();//기본값으로 채워
				boardbean.setBoard_num(rs.getInt("board_num"));//글번호
				boardbean.setBoard_name(rs.getString("board_name"));//글작성
				boardbean.setBoard_subject(rs.getString("board_subject"));//글제목
				boardbean.setBoard_content(rs.getString("board_content"));//글내용
				boardbean.setBoard_file(rs.getString("board_file"));//첨부파일
				
				boardbean.setBoard_re_ref(rs.getInt("board_re_ref"));//관련글 번호
				boardbean.setBoard_re_lev(rs.getInt("board_re_lev"));//답글 레벨
				boardbean.setBoard_re_seq(rs.getInt("board_re_seq"));//관련글 중 출력순서
				
				boardbean.setBoard_readcount(rs.getInt("board_readcount"));//조회수
				boardbean.setBoard_date(rs.getDate("board_date"));//작성일
				
				articleList.add(boardbean);
			} 
			
		} catch (Exception e) {
			System.out.println("getboardList 에러 : "+e);//e:예외종류+예외메세지
			e.printStackTrace();
		} finally {
			close(pstmt);
			close(rs);
		}
		return articleList;//조회한 10개의 행
	}
	
	//4. '글번호'로 해당글을 조회하여 조회수 1증가
	public int updateReadCount(int board_num) {
		int updateCount =0;//지역변수 초기화
		
		String sql ="update board set = board_readcount=board_readcount+1 where board_num= ?";
		try {
			pstmt= con.prepareStatement(sql);
			updateCount = pstmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("setReadCountUpdate 에러 : "+e);//e:예외종류+예외메세지
			e.printStackTrace();
		} finally {
			close(pstmt);
		}
		return updateCount;
	}
	
	//5. '글번호'로 '글 하나의 정보'를 조회해서 'BoardBean객체로 반환'
	public Boardbean selectArticle(int board_num) {
		Boardbean boardbean = null;
		
//		String sql ="select * from board where board_num = "+board_num;
		String sql ="select * from board where board_num = ? ";
		
		try {
			pstmt = con.prepareStatement(sql);
			
			pstmt.setInt(1, board_num);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				boardbean = new Boardbean();
				boardbean.setBoard_num(rs.getInt("board_num"));
				
				boardbean.setBoard_name(rs.getString("board_name"));
				boardbean.setBoard_subject(rs.getString("board_subject"));
				boardbean.setBoard_content(rs.getString("board_content"));
				boardbean.setBoard_file(rs.getString("board_file"));
				
				boardbean.setBoard_re_ref(rs.getInt("board_re_ref"));
				boardbean.setBoard_re_lev(rs.getInt("board_re_lev"));
				boardbean.setBoard_re_seq(rs.getInt("board_re_seq"));
				boardbean.setBoard_readcount(rs.getInt("board_readcount"));
				boardbean.setBoard_date(rs.getDate("board_date"));
			}
			
		} catch (Exception e) {
			System.out.println("getDetail 에러 : "+e);//e:예외종류+예외메세지
			e.printStackTrace();
		} finally {
			close(pstmt);
			close(rs);
		}
		
		
		return boardbean;
		
	}
	
	//6. 답변글쓰기 폼에서 답변관련 내용을 담은 Boardbean객체를 board테이블에 추가!
	//'글쓴이, 비밀번호, 제목, 내용' 만 답변글 정보이고 '나머지 값들'은 원글의 내용 그대로 
	public int insertReplyArticle(Boardbean article){//보드 빈 객체를 매개값으로 받아야 한다
		String board_max_sql ="select ifnull(max(board_num),0)+1 from board";
		int num = 0;
		String sql ="";
		
		int re_ref = article.getBoard_re_ref(); //원글의 그룹 번호
		int re_seq = article.getBoard_re_seq(); //원글에서 답변글이 몇 번째 아래에 놓일 것인지 위치를 결정해주는 값
		int re_lev = article.getBoard_re_lev(); //들여쓰기 
		article.getBoard_re_ref(); //원글의 그룹번호
		int updateCount = 0; 
		
		int insertCount = 0; //밑에서 선언하면 안되닌깐 여서 선언 한다 
		try {
			pstmt = con.prepareStatement(board_max_sql);
			rs= pstmt.executeQuery();
			
		
		//if(rs.next()) num = rs.getInt(1)+1;//---▶ 교재내용
		//else num = 1; //아므골도 없으면 답변글 번호를 1로 시작  //---▶ 교재내용
			if(rs.next()) num = rs.getInt(1); //num : 답변글 번호 ---▶ 수정함
			
			/*
			 * ★★★★"최신답변글을 원글 바로 아래로 위치 하도록 하기 위해"
			 * 원글의 답변글만 찾아서 board_re_seq를 각각 1증가 
			 * 
			 * (예1)원글이 0이면      원글 0              원글 0 
			 * ----------------------------------------------
			 * 	 			 			   답변글 1(최신답변글)
			 * 	 답변글 1 ->   답변글 2   -> 답변글 2
			 * 	  답변글 1	->	   답변글 2	  -> 답변글 2
			 * 
			 *  (예2)원글이 "답변글"이면
			 *  답변글 1 		답변글 1		답변글 1(원글) 
			 * ----------------------------------------------
			 * 	 			 			 답변글 2(최신답변글)
			 * 	 답변글 2 ->   답변글 3   -> 답변글 3
			 * 	  답변글 2	->	   답변글 3	  -> 답변글 3
			 */
			sql ="update board setboard_re_seq=board_re_seq+1 where board_re_ref =? and road_re_seq > ? ";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, re_ref);//원글의  그룹번호
			pstmt.setInt(2, re_seq);//원글의  위치
			
			updateCount = pstmt.executeUpdate();//업데이트가 성공 하면 1을 리턴 받는다 
			
			if(updateCount > 0 ) commit(con);
			else rollback(con);
			/******  답변글 등록	****************************************/
			re_lev = re_lev+1;//원글레벨(0)+1  원글레벨이(1)+1 
			re_seq = re_seq+1;//원글위치(0)+1	원글위치이(1)+1 답변의 답변글 처럼 될수 있다 

			//★주위★ sql += "";하면 안됌!!!! 왜? 위에  sql="update 뭐시기" 인데  += 하면 이어 붙이겠다는것이다!!
			sql ="insert into board values(?,?,?,?,?,?,?,?,?,?,now())";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, num);//위에서 계산한 글 번호 
			
			//아래 4개의 값(글쓴이, 비번, 제목, 내용)은 전송받은 값을 그대로 가져와
			pstmt.setString(2, article.getBoard_name());
			pstmt.setString(3, article.getBoard_pass());
			pstmt.setString(4, article.getBoard_subject());
			pstmt.setString(5, article.getBoard_content());
			
			pstmt.setString(6,"");//답변글 폼에는 파일을 업로드 하는 부분이 없음!!
			
			pstmt.setInt(7, re_ref);//원글과 같은 그룹번호를 넣어야 엮을수 있다
			pstmt.setInt(8, re_lev);//위에 있는 원글+1 셋팅 해온걸 가져왔다 왜?? 원글보다 한번더 들여쓰기 위해
			pstmt.setInt(9, re_seq);//원글+1 들여 쓰기 하기 위해   (원글 바로 아래에 위치 함)
			pstmt.setInt(10, 0); // 조회수 !
			
			 insertCount = pstmt.executeUpdate(); //시발 여서 int inserCount 했다가 안되닌깐 위에서 선언 해야한다 ㅅㅂ
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("boardReply 에러 : " +e);
		}finally {
			close(rs);
			close(pstmt);
		}
		return insertCount;
		
	}
}
