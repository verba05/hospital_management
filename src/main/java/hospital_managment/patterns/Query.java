package hospital_managment.patterns;

import java.util.ArrayList;
import java.util.List;

public class Query {
     public enum Operator {
        EQUALS("="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        LIKE("LIKE");

        private final String sql;

        Operator(String sql) {
            this.sql = sql;
        }

        public String toSql() {
            return sql;
        }
    }

    public class Criteria {
        public final String field;
        public final Operator operator;
        public final Object value;

        public Criteria(String field, Operator operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
    }
    private List<Criteria> criteriaList = new ArrayList<>();
    private List<String> orderList = new ArrayList<>();
    private Integer limit;
    private Integer offset;

    public Query where(String field, Operator op, Object value) {
        criteriaList.add(new Criteria(field, op, value));
        return this;
    }

    public Query orderBy(String field) {
        orderList.add(field);
        return this;
    }

    public Query limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Query offset(int offset) {
        this.offset = offset;
        return this;
    }

    public List<Criteria> criteria() { return criteriaList; }
    public List<String> orders() { return orderList; }
    public Integer limit() { return limit; }
    public Integer offset() { return offset; }
}

