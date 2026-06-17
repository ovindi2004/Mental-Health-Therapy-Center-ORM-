package lk.ijse.mental_health_therapy.dao;


import java.util.List;

public interface CrudDAO<T, ID> extends SuperDAO{
    boolean save(T entity) throws Exception;
    boolean update(T entity);
    boolean delete(ID id);
    T findById(ID id);
    List<T> findAll();
}
