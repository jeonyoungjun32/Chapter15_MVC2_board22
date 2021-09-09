// BoardFrontController 에서 왔다
//이 클레스는 '답변글 등록 요청' 처리하는 Action
package action;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import svc.BoardReplyProService;
import vo.ActionForward;
import vo.Boardbean;

public class BoardReplayProAction implements Action {


	@Override
	public ActionForward exeute(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
		// TODO Auto-generated method stub
		String nowPage = request.getParameter("page");// 원래 있던 페이지로 돌아가기 위해

		//boardbean객세생성 하여 (기본값으로 채워짐)
		Boardbean article = new Boardbean();
		
		//답변글 작성 품에서 작성한 파라미터 값들을 전송 받아서  그 값들을 boardbean속성에 값에 다시 설정한다.
		article.setBoard_num(Integer.parseInt(request.getParameter("board_num")));
		
		//사용자가 실제 입력한 값들(이름, 비번, 제목, 내용) -------
		article.setBoard_name(request.getParameter("board_name"));
		article.setBoard_pass(request.getParameter("board_pass"));
		article.setBoard_subject(request.getParameter("board_subject"));
		article.setBoard_content(request.getParameter("board_content"));
		
		//나머지 4개는 원글의 정보를 그대로 얻어옴!! ----------------
		article.setBoard_re_ref(Integer.parseInt(request.getParameter("board_num")));
		article.setBoard_re_lev(Integer.parseInt(request.getParameter("board_num")));
		article.setBoard_re_seq(Integer.parseInt(request.getParameter("board_num")));
		
		BoardReplyProService BoardReplyProService = new BoardReplyProService();
		//서비스를 거지고 DAO도 거치고 난 후 나온다
		
		BoardReplyProService boardReplyProService = new BoardReplyProService();
		boolean isReplySuccess = boardReplyProService.replyArticle(article);
		
		ActionForward forward = null;
		if(isReplySuccess) {//답변글 등록 성공하면
			 forward = new ActionForward();
			 //원래 있던 페이지로 되돌아가야 함
			 forward.setPath("boardList.bo?page="+ nowPage);
		}else {//답변글 등록 실패하면 "경고"
			response.setContentType("text/htm;charset=UTF-8");
			response.getWriter();
			
			out.println("<script>");
			out.println("alert(답변글 등록 실해);");
			out.println("history.back();");
			out.println("<script>");
			
		}
		
		return null;
	}

}
