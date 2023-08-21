package mate.academy.dao.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@Dao
public class MovieSessionDaoImpl implements MovieSessionDao {
    @Override
    public MovieSession add(MovieSession movieSession) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            Long movieSessionId = (Long) session.save(movieSession);
            movieSession.setId(movieSessionId);
            transaction.commit();
            return movieSession;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Couldn't add a movie session " + movieSession, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Optional<MovieSession> get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> getMovieSession = session.createQuery("FROM MovieSession ms "
                    + "LEFT JOIN FETCH ms.cinemaHall "
                    + "LEFT JOIN FETCH ms.movie "
                    + "WHERE ms.id = :id",
                    MovieSession.class);
            getMovieSession.setParameter("id", id);
            return getMovieSession.uniqueResultOptional();
        } catch (Exception e) {
            throw new DataProcessingException("Couldn't get a cinema hall by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> getAvailableMovieSessions = session.createQuery(
                    "FROM MovieSession ms "
                    + "LEFT JOIN FETCH ms.movie "
                    + "LEFT JOIN FETCH ms.cinemaHall "
                    + "WHERE ms.movie.id = :movieId "
                    + "AND YEAR(ms.showTime) = :year "
                    + "AND MONTH(ms.showTime) = :month "
                    + "AND DAY(ms.showTime) = :day",
                    MovieSession.class);
            getAvailableMovieSessions.setParameter("year", date.getYear());
            getAvailableMovieSessions.setParameter("month", date.getMonthValue());
            getAvailableMovieSessions.setParameter("day", date.getDayOfMonth());
            getAvailableMovieSessions.setParameter("movieId", movieId);
            return getAvailableMovieSessions.getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Couldn't find available sessions for movie "
                    + "with id:" + movieId + ", and date: " + date, e);
        }
    }
}
