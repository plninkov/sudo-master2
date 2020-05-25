package web;

import quiz.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * ToDo Add query parameters to HTTP GET /quizzes
 * ToDo Create quiz with HTTP POST, returning the location
 * ToDo PUT /quizzes/{id} - response 200 if updated; 201 if created
 * ToDo Get rid of parameters in to achieve PATCH for pure REST
 */
@Path("/QuizService")
@Consumes(MediaType.APPLICATION_XML)
public class WebQuiz {

    @GET
    @Path("/quizzes/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getQuiz(@PathParam("id") int id) throws InvalidRequestException {
        return Response.status(200)
                .entity(QuizProcessor.getQuiz(id))
                // .type(MediaType.APPLICATION_XML)
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
    @Path("/solutions/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getSolution(@PathParam("id") int id) throws InvalidRequestException {
        return Response.status(200)
                .entity(QuizProcessor.getSolution(id))
                .build();
    }

    @POST
    @Path("/quizzes")
    @Produces(MediaType.APPLICATION_XML)
    public Response createQuiz(Quiz quiz) throws InvalidRequestException {
        QuizProcessor.createQuiz(quiz);
        return Response.status(201)
                .entity(quiz)
                .link(URI.create("/quizzes/"+ quiz.getQuizID()), "Location")
                .build();
    }

    @PUT
    @Path("/quizzes/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response updateQuiz(@PathParam("id") int id,
                               @QueryParam("updateID") String newIdStr,
                               @QueryParam("UpdateName") String nameStr,
                               @QueryParam("publish") String pubishStr) throws InvalidRequestException {
        return Response.status(201)
                .entity(QuizProcessor.updateQuiz(id, newIdStr, nameStr, pubishStr))
                .build();
    }

    @PATCH
    @Path("/quizzes/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response changeQuiz(@PathParam("id") int id, Line[] lines) throws InvalidRequestException {
        return Response.status(201)
                .entity(QuizProcessor.changeQuiz(id, lines))
                .build();
    }

    @DELETE
    @Path("/quizzes/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteQuiz(@PathParam("id") int id) throws InvalidRequestException {
        QuizProcessor.deleteQuiz(id);
        return Response.status(204)
                .build();
    }
}
