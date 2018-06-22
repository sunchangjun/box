package framework.common.jpa;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
public interface Criterion {  
    
    public enum Operator {  
        EQ, NE, LIKE, GT, LT, GTE, LTE, AND, OR  
    }  
    
    public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query,
                                 CriteriaBuilder builder);
}
