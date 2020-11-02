package interbox.data.sql;

import java.util.Arrays;


class SqlGen {

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
        } else {
            throw new QbException("empty from clause");
        }
        // join clauses
        for (JoinClause join : select.joins) {
            GenCtx joinCtx = new GenCtx(ctx);
            visitJoin(join, joinCtx);
            sb.append(' ').append(joinCtx.result);
        }
        // where clause
        if (select.where != null) {
            GenCtx whereCtx = new GenCtx(ctx);
            select.where.negated = false;
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

    public void visitDelete(QbDelete delete, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        StringBuilder sb = new StringBuilder();
        sb.append("delete from ").append(delete.fromTable);
        if (delete.where != null) {
            GenCtx wc = new GenCtx(ctx);
            delete.where.negated = false;
            visitCond(delete.where, wc);
            sb.append(" where ").append(wc.result);
        }
        ctx.result = sb.toString();
    }

    public void visitUpdate(QbUpdate update, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(update.table).append(" set ");
        if (update.assignments.isEmpty())
            throw new QbException("no data to update");
        for (int i = 0; i < update.assignments.size(); i++) {
            if (i > 0) sb.append(',');
            Assignment a = update.assignments.get(i);
            if (a instanceof SAssign) {
                SAssign sa = (SAssign) a;
                sb.append(sa.field).append('=').append(sa.expr);
            } else if (a instanceof OAssign) {
                OAssign oa = (OAssign) a;
                sb.append(oa.field).append("=?");
                ctx.addParam(oa.value, oa.type);
            }
        }
        if (update.where != null) {
            GenCtx wc = new GenCtx(ctx);
            update.where.negated = false;
            visitCond(update.where, wc);
            sb.append(" where ").append(wc.result);
        }
        ctx.result = sb.toString();
    }

    public void visitInsert(QbInsert insert, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        if (insert.assignments.isEmpty())
            throw new QbException("no data to insert");
        for (int i = 0; i < insert.assignments.size(); i++) {
            if (i > 0) {
                fields.append(',');
                values.append(',');
            }
            Assignment a = insert.assignments.get(i);
            if (a instanceof SAssign) {
                SAssign sa = (SAssign) a;
                fields.append(sa.field);
                values.append(sa.expr);
            } else if (a instanceof OAssign) {
                OAssign oa = (OAssign) a;
                fields.append(oa.field);
                values.append('?');
                ctx.addParam(oa.value, oa.type);
            }
        }
        ctx.result = "insert into " + insert.table + " (" +
                fields.toString() + ") values (" +
                values.toString() + ')';
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
        } else {
            throw new QbException("empty join clause");
        }
        GenCtx onCtx = new GenCtx(ctx);
        join.on.negated = false;
        visitCond(join.on, onCtx);
        sb.append(onCtx.result);
        ctx.result = sb.toString();
    }

    public void visitCond(QbCondClause cond, Object obj) {
        if (cond != null && cond.isEnabled()) {
            if (cond instanceof ExprCond) {
                visitExprCond((ExprCond) cond, obj);
            } else if (cond instanceof NotCond) {
                visitNotCond((NotCond) cond, obj);
            } else if (cond instanceof LogicalCond) {
                visitLogicalCond((LogicalCond) cond, obj);
            } else if (cond instanceof LogicalCondList) {
                visitLogicalCondList((LogicalCondList) cond, obj);
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
        if (cond.negated)
            ctx.result = "not (" + cond.expr + ')';
        else
            ctx.result = cond.expr;
    }

    public void visitNotCond(NotCond cond, Object obj) {
        cond.cond1.negated = !cond.negated; // pass down this NOT
        // else not-not=do-nothing
        visitCond(cond.cond1, obj);
    }

    public void visitLogicalCond(LogicalCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        if (Arrays.asList(
                QueryBuilder.Logical.AND,
                QueryBuilder.Logical.OR).contains(cond.logical)) {
            if (cond.negated) {
                // not (a and b)=not a or not b
                QueryBuilder.Logical l = cond.logical == QueryBuilder.Logical.AND
                        ? QueryBuilder.Logical.OR :
                        QueryBuilder.Logical.AND;
                String op = l.name().toLowerCase();
                GenCtx c1 = new GenCtx(ctx);
                cond.cond1.negated = true;
                visitCond(cond.cond1, c1);
                GenCtx c2 = new GenCtx(ctx);
                cond.cond2.negated = true;
                visitCond(cond.cond2, c2);
                ctx.result = '(' + c1.result + ") " + op + " (" + c2.result + ')';
            } else {
                String op = cond.logical.name().toLowerCase();
                GenCtx c1 = new GenCtx(ctx);
                cond.cond1.negated = false;
                visitCond(cond.cond1, c1);
                GenCtx c2 = new GenCtx(ctx);
                cond.cond2.negated = false;
                visitCond(cond.cond2, c2);
                ctx.result = '(' + c1.result + ") " + op + " (" + c2.result + ')';
            }
        } else {
            throw new QbException("illegal logical operator");
        }
    }

    public void visitLogicalCondList(LogicalCondList condList, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String op = condList.logical.name().toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (QbCondClause cond : condList.condList) {
            if (cond.isEnabled()) {
                if (sb.length() > 0)
                    sb.append(' ').append(op).append(' ');
                GenCtx c = new GenCtx(ctx);
                cond.negated = false;
                visitCond(cond, c);
                sb.append('(').append(c.result).append(')');
            }
        }
        if (condList.negated)
            ctx.result = "not (" + sb.toString() + ')';
        else
            ctx.result = sb.toString();
    }

    public void visitSCompCond(SCompCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String e = cond.expr;
        if (cond.negated) {
            if (cond.asExpr) {
                e += cond.comp.negate().token + cond.str;
            } else if (cond.str == null) {
                e += " is not null";
            } else {
                e += cond.comp.negate().token + '?';
                ctx.addParam(cond.str);
            }
        } else {
            if (cond.asExpr) {
                e += cond.comp.token + cond.str;
            } else if (cond.str == null) {
                e += " is null";
            } else {
                e += cond.comp.token + '?';
                ctx.addParam(cond.str);
            }
        }
        ctx.result = e;
    }

    public void visitOCompCond(OCompCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String e = cond.expr;
        if (cond.negated) {
            if (cond.value == null) {
                e += " is not null";
            } else if (cond.value instanceof QbSelect) {
                GenCtx sc = new GenCtx(ctx);
                visitSelect((QbSelect) cond.value, sc);
                e += cond.comp.negate().token + '(' + sc.result + ')';
            } else {
                e += cond.comp.negate().token + '?';
                ctx.addParam(cond.value);
            }
        } else {
            if (cond.value == null) {
                e += " is null";
            } else if (cond.value instanceof QbSelect) {
                GenCtx sc = new GenCtx(ctx);
                visitSelect((QbSelect) cond.value, sc);
                e += cond.comp.token + '(' + sc.result + ')';
            } else {
                e += cond.comp.token + '?';
                ctx.addParam(cond.value);
            }
        }
        ctx.result = e;
    }

    public void visitInCond(InCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String e = cond.expr;
        if (cond.negated)
            e += " not in ";
        else
            e += " in ";
        GenCtx inCtx = new GenCtx(ctx);
        visitSelect(cond.subquery, inCtx);
        e += '(' + inCtx.result + ')';
        ctx.result = e;
    }

    public void visitExistsCond(ExistsCond cond, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        GenCtx exCtx = new GenCtx(ctx);
        visitSelect(cond.subquery, exCtx);
        String op = cond.negated ? "not exists" : "exists";
        ctx.result = op + " (" + exCtx.result + ')';
    }

    public void visitGroupBy(GroupByClause groupBy, Object obj) {
        GenCtx ctx = (GenCtx) obj;
        String gb = "group by " + groupBy.field;
        if (groupBy.having != null) {
            GenCtx c = new GenCtx(ctx);
            groupBy.having.negated = false;
            visitCond(groupBy.having, c);
            gb += " having " + c.result;
        }
        ctx.result = gb;
    }

}
