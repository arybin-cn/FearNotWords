package info.arybin.fearnotwords.model.orm;

import org.litepal.annotation.Column;


/**
 * A Plan is an aggregation for many Expression(eg. "GRE")
 * Note: We should not build many-to-many relation between Expression and Plan,
 * which will lead to lots of cost in performance due to ORM...
 * <p>
 * Here we build "one-to-one" relation(in fact a map)
 * between Expression and Plan to avoid the potential issue
 */
public class Plan extends LocalizedORM {
    @Column(nullable = false)
    public Expression expression;
}