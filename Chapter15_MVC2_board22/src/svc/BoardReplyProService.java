//'답변정보 등록 요청' 처리하는 Service클레스

package svc;

import static db.Jdbcutil.commit;
import static db.Jdbcutil.getConnection;

import java.sql.Connection;

import dao.BoardDAO;
import vo.Boardbean;

public class BoardReplyProService {

	// 1. 커넥션 객체 가지고 와야 한다
	public boolean replyArticle(Boardbean article) {
		Connection con = getConnection();
		BoardDAO boardDAO = BoardDAO.getInstance();
		boardDAO.setConnection(con);//이제 db연결 됬다잉!!
		
		int insertCount = boardDAO.insertReplyArticle(article); // BoardDAO 여 안에 있는 6번 이다
		
		boolean isReplySuccess = false;
		
		if(insertCount > 0) {
			commit(con);
			isReplySuccess =true;
		}else {
			rollbacke(con);
			
		}
		
		return isReplySuccess;

	}
	
}
