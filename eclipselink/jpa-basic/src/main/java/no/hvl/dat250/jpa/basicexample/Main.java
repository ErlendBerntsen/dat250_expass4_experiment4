package no.hvl.dat250.jpa.basicexample;

import com.google.gson.Gson;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import static spark.Spark.*;

public class Main {
    private static final String PERSISTENCE_UNIT_NAME = "todos";
    private static EntityManagerFactory factory;

    public static void main(String[] args) {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = factory.createEntityManager();


        if (args.length > 0) {
            port(Integer.parseInt(args[0]));
        } else {
            port(8080);
        }

        Todo todo1 = new Todo();
        todo1.setSummary("Summary1");
        todo1.setDescription("Description1");
        Todo todo2 = new Todo();
        todo2.setSummary("Summary2");
        todo2.setDescription("Description2");
        em.getTransaction().begin();
        em.persist(todo1);
        em.persist(todo2);
        em.getTransaction().commit();


        after((req, res) -> {
            res.type("application/json");
        });

        get("/todos", (req, res) -> {
            Gson gson = new Gson();
            List<Todo> todos = em.createQuery("select t from Todo t").getResultList();
            return gson.toJson(todos);
        });

        get("/todos/:id", (req, res) -> {
            try{
                Todo todo = (Todo) em.createQuery("select t from Todo t where t.id=:id")
                        .setParameter("id", Long.parseLong(req.params(":id")))
                        .getSingleResult();
                return todo.toJson();
            }catch (NoResultException e){
                res.status(404);
                return ("Couldn't find todo " + req.params(":id"));
            }
        });

        post("/todos", (req, res) -> {
            Gson gson = new Gson();
            Todo todo = gson.fromJson(req.body(), Todo.class);
            em.getTransaction().begin();
            em.persist(todo);
            em.getTransaction().commit();
            res.status(201);
            return todo.toJson();
        });

        put("/todos/:id", (req, res) -> {
            Gson gson = new Gson();
            try{
                Todo todo = (Todo) em.createQuery("select t from Todo t where t.id=:id")
                        .setParameter("id", Long.parseLong(req.params(":id")))
                        .getSingleResult();
                Todo updatedTodo = gson.fromJson(req.body(), Todo.class);
                todo.setSummary(updatedTodo.getSummary());
                todo.setDescription(updatedTodo.getDescription());
                return todo.toJson();
            }catch (NoResultException e){
                Todo todo = gson.fromJson(req.body(), Todo.class);
                em.getTransaction().begin();
                em.persist(todo);
                em.getTransaction().commit();
                res.status(201);
                return todo.toJson();
            }
        });

        delete("/todos/:id", (req, res) -> {
            try{
                Todo todo = (Todo) em.createQuery("select t from Todo t where t.id=:id")
                        .setParameter("id", Long.parseLong(req.params(":id")))
                        .getSingleResult();
                em.getTransaction().begin();
                em.remove(todo);
                em.getTransaction().commit();
                res.status(202);
                return ("Deleted todo " + req.params(":id"));
            }catch (NoResultException e){
                res.status(404);
                return ("Couldn't find todo " + req.params(":id"));
            }
        });
    }
}
