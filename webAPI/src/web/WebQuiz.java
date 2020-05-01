package web;

import quiz.InvalidRequestException;
import quiz.Line;
import quiz.Quiz;
import quiz.QuizProcessor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/QuizService")
@Consumes(MediaType.APPLICATION_XML)
public class WebQuiz {

    @POST
    @Path("/quiz")
    @Produces(MediaType.APPLICATION_XML)
    public Response createQuiz(Quiz quiz) throws InvalidRequestException {
        QuizProcessor.createQuiz(quiz);
        return Response.status(201)
                .entity(quiz)
                .build();
    }

    @GET
    @Path("/quiz/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getQuiz(@PathParam("id") int id) throws InvalidRequestException {
        return Response.status(200)
                .entity(QuizProcessor.getQuiz(id))
                // .type(MediaType.APPLICATION_XML)
                .build();
    }

    @DELETE
    @Path("/quiz/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteQuiz(@PathParam("id") int id) throws InvalidRequestException {
        return Response.status(200)
                .entity(QuizProcessor.deleteQuiz(id))
                .build();
    }

    @PUT
    @Path("/quiz/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response updateQuiz(@PathParam("id") int id,
                               @QueryParam("updateID") String newIdStr,
                               @QueryParam("UpdateName") String nameStr,
                               @QueryParam("publish") String pubishStr) throws InvalidRequestException {
        return Response.status(200)
                .entity(QuizProcessor.updateQuiz(id, newIdStr, nameStr, pubishStr))
                .build();
    }

    @PATCH
    @Path("/quiz/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response changeQuiz(@PathParam("id") int id, Line[] lines) throws InvalidRequestException {
        return Response.status(201)
                .entity(QuizProcessor.changeQuiz(id, lines))
                .build();
    }

    @GET
    @Path("/quizzes")
    @Produces(MediaType.APPLICATION_XML)
    public Response getAllQuizzes() throws InvalidRequestException {
        return Response.status(200)
                .entity(QuizProcessor.getAllQuizzes())
                .build();
    }

    @GET
    @Path("/solutions")
    @Produces(MediaType.APPLICATION_XML)
    public Response getAllSolutions() throws InvalidRequestException {
        return Response.status(200)
                .entity(QuizProcessor.getAllSolutions())
                .build();
    }

    @GET
    @Path("/solution/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getSolution(@PathParam("id") int id) throws InvalidRequestException {
        return Response.status(200)
                .entity(QuizProcessor.getSolution(id))
                .build();
    }

}
