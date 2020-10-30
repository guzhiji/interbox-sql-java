package interbox.data.sql;

import java.util.ArrayList;
import java.util.List;


class GenCtx {
    String result;
    final List<Param> params;
    final GenCtx parent;

    GenCtx() {
        this(new ArrayList<>());
    }

    GenCtx(List<Param> params) {
        this.parent = null;
        this.params = params;
    }

    GenCtx(GenCtx ctx) {
        this.parent = ctx;
        this.params = ctx.params;
    }

    void addParam(Object value, int type) {
        Param p = new Param();
        p.value = value;
        p.type = type;
        this.params.add(p);
    }

    void addParam(Object value) {
        Param p = new Param();
        p.value = value;
        this.params.add(p);
    }
}
