package interbox.data.sql;

import java.util.Arrays;


class SelectGen {

    public void visitSelect(QbSelect select, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        if (select.distinct)
            sb.append(" distinct ");
        // fields
        if (select.fields.isEmpty()) {
            sb.append('*');
        } else {
            for (int i = 0; i < select.fields.size(); i++) {
                FieldExpr field = select.fields.get(i);
                if (i > 0) sb.append(',');
                sb.append(field.expr);
                if (field.alias != null)
                    sb.append(" as ").append(field.alias);
            }
        }
        // from clause
        if (select.fromTable != null) {
            sb.append(" from ").append(select.fromTable);
            if (select.fromAlias != null)
                sb.append(" as ").append(select.fromAlias);
        } else if (select.fromSubquery != null) {
            GenCtx subCtx = new GenCtx(ctx);
            visitSelect(select.fromSubquery, subCtx);
            sb.append(" from ").append('(').append(subCtx.result).append(')')
                    .append(" as ").append(select.fromAlias);
        } // TODO empty from clause
        // join clauses
        for (JoinClause join : select.joins) {
            GenCtx joinCtx = new GenCtx(ctx);
            visitJoin(join, joinCtx);
            sb.append(' ').append(joinCtx.result);
        }
        // where clause
        if (select.where != null) {
            GenCtx whereCtx = new GenCtx(ctx);
            visitCond(select.where, whereCtx);
            sb.append(" where ").append(whereCtx.result);
        }
        // group-by clauses
        for (GroupByClause gb : select.groupBys) {
            GenCtx gbCtx = new GenCtx(ctx);
            visitGroupBy(gb, gbCtx);
            sb.append(' ').append(gbCtx.result);
        }
        // order-bys
        if (!select.orderBys.isEmpty()) {
            sb.append(" order by ");
            for (int i = 0; i < select.orderBys.size(); i++) {
                OrderBy ob = select.orderBys.get(i);
                if (i > 0) sb.append(',');
                sb.append(ob.field).append(' ')
                        .append(ob.order.name().toLowerCase());
            }
        }
        ctx.result = sb.toString();
    }

    public void visitJoin(JoinClause join, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        StringBuilder sb = new StringBuilder();
        sb.append(join.type.name().toLowerCase()).append(" join ");
        if (join.table != null) {
            sb.append(join.table);
            if (join.alias != null)
                sb.append(" as ").append(join.alias).append(" on ");
        } else if (join.subquery != null) {
            GenCtx subCtx = new GenCtx(ctx);
            visitSelect(join.subquery, subCtx);
            sb.append('(')
                    .append(subCtx.result)
                    .append(") as ")
                    .append(join.alias)
                    .append(" on ");
        } // TODO empty join clause
        GenCtx onCtx = new GenCtx(ctx);
        visitCond(join.on, onCtx);
        sb.append(onCtx.result);
        ctx.result = sb.toString();
    }

    public void visitCond(QbCondClause cond, Object obj) {
        if (cond != null && cond.isEnabled()) {
            if (cond instanceof ExprCond) {
                visitExprCond((ExprCond) cond, obj);
            } else if (cond instanceof RelCond) {
                visitRelCond((RelCond) cond, obj);
            } else if (cond instanceof RelCondList) {
                visitRelCondList((RelCondList) cond, obj);
            } else if (cond instanceof SCompCond) {
                visitSCompCond((SCompCond) cond, obj);
            } else if (cond instanceof OCompCond) {
                visitOCompCond((OCompCond) cond, obj);
            } else if (cond instanceof InCond) {
                visitInCond((InCond) cond, obj);
            } else if (cond instanceof ExistsCond) {
                visitExistsCond((ExistsCond) cond, obj);
            }
        }
    }

    public void visitExprCond(ExprCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        ctx.result = cond.expr;
    }

    public void visitRelCond(RelCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        if (Arrays.asList(
                QueryBuilder.Rel.AND,
                QueryBuilder.Rel.OR).contains(cond.rel)) {
            String op = cond.rel.name().toLowerCase();
            GenCtx c1 = new GenCtx(ctx);
            visitCond(cond.cond1, c1);
            GenCtx c2 = new GenCtx(ctx);
            visitCond(cond.cond2, c2);
            ctx.result = '(' + c1.result + ") " + op + " (" + c2.result + ')';
        } else if (cond.rel == QueryBuilder.Rel.NOT) {
            // TODO not operator
        }
    }

    public void visitRelCondList(RelCondList condList, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String op = condList.rel.name().toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (QbCondClause cond : condList.condList) {
            if (cond.isEnabled()) {
                if (sb.length() > 0)
                    sb.append(' ').append(op).append(' ');
                GenCtx c = new GenCtx(ctx);
                visitCond(cond, c);
                sb.append('(').append(c.result).append(')');
            }
        }
        ctx.result = sb.toString();
    }

    public void visitSCompCond(SCompCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String e = cond.expr;
        if (cond.asExpr) {
            e += cond.comp.token + cond.str;
        } else if (cond.str == null) {
            e += " is null";
        } else {
            e += cond.comp.token + '?';
            ctx.params.add(cond.str);
        }
        ctx.result = e;
    }

    public void visitOCompCond(OCompCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String e = cond.expr;
        if (cond.value == null) {
            e += " is null";
        } else if (cond.value instanceof QbSelect) {
            GenCtx sc = new GenCtx(ctx);
            visitSelect((QbSelect) cond.value, sc);
            e += cond.comp.token + '(' + sc.result + ')';
        } else {
            e += cond.comp.token + '?';
            ctx.params.add(cond.value);
        }
        ctx.result = e;
    }

    public void visitInCond(InCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String e = cond.expr + " in (";
        GenCtx inCtx = new GenCtx(ctx);
        visitSelect(cond.subquery, inCtx);
        e += inCtx.result + ')';
        ctx.result = e;
    }

    public void visitExistsCond(ExistsCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        GenCtx exCtx = new GenCtx(ctx);
        visitSelect(cond.subquery, exCtx);
        ctx.result = "exists (" + exCtx.result + ")";
    }

    public void visitGroupBy(GroupByClause groupBy, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String gb = "group by " + groupBy.field;
        if (groupBy.having != null) {
            GenCtx c = new GenCtx(ctx);
            visitCond(groupBy.having, c);
            gb += " having " + c.result;
        }
        ctx.result = gb;
    }

}
