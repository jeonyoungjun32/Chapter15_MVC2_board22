<!-- 답변글 쓰기 화면을 보여주는 뷰페이지(JSP책 642p 아래그림 참조) -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="vo.Boardbean" %>
<%
   Boardbean article = (Boardbean)request.getAttribute("article");//원글 정보
   String nowPage = (String)request.getAttribute("page");//원래 페이지로 돌아가기 위한 페이지 번호
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>MVC2 게시판</title>
<style>
#writeFrom {
width:500px;
overflow:hidden;
border:1px solid red;
margin: auto;
}

h2 {
text-align: center;
}

table {
margin: auto;
width: 450px;
}
/*테이블의 왼쪽셀*/
.td_left{
width: 150px;
background-color: orange;
}
/*테이블의 오른쪽셀*/
.td_right{
width: 300px;
background-color: skyblue;
}
#commandCell {
   text-align: center;
}
</style>
</head>
<body>
   <section id="writeForm">
      <h2>게시판 글등록(답변)</h2>
      <form action="boardReplyPro.bo" method="post" name="boardform">
      <!-- hidden이유? 답변글등록 클릭하면 아래 2개의값도 함께 전송하기위해
      page : 원래있던 페이지로 돌아가기위해
      board_num : 원글 조회하기위해 -->
      <input type="hidden" name="page" value="nowPage"/>
      <input type="hidden" name="board_num" value="<%=article.getBoard_num()%>"/>
      <!--hidden 이유? [e답변글등록]클릭하면!'원글의 그룹, 들여쓰기, 위치값은 보여줄 필요없이 전송하기 위해 
      답변글의 그룹, 들여쓰기, 위치를 원글의 그룹, 들여스기, 위치값으로 계산 하기 때문에-->
      
      <input type="hidden" name="board_re_ref" value="<%=article.getBoard_re_ref()%>"/>
      <input type="hidden" name="board_re_lev" value="<%=article.getBoard_re_lev()%>"/>
      <input type="hidden" name="board_re_seq" value="<%=article.getBoard_re_seq()%>"/>
         <table>
            <tr>
               <td><label for="board_name">글쓴이</label></td>
               <td><input type="text" name="board_name" id="board_name"></td>
            </tr>
            <tr>
               <td><label for="board_pass">비밀번호</label></td>
               <td><input type="password" name="board_pass" id="board_pass"></td>
            </tr>
            <tr>
               <td><label for="board_subject">제목</label></td>
               <td><input type="text" name="board_subject" id="board_subject"></td>
            </tr>
            <tr>
               <td><label for="board_content">내용</label></td>
               <td><textarea name="board_content" id="board_content" cols="40" rows="15"></textarea></td>
            </tr>
         </table>
         <section id="commandCell">
            <input type="submit" value="답변글등록"/>&nbsp;&nbsp;
            <input type="reset" value="다시작성"/>
         </section>
      </form>
   </section>
</body>
</html>