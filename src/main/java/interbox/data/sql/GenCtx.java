package interbox.data.sql;

import java.util.List;


class GenCtx {
    String result;
    final List<Object> params;
    final GenCtx parent;

    GenCtx(List<Object> params) {
        this.parent = null;
        this.params = params;
    }

    GenCtx(GenCtx ctx) {
        this.parent = ctx;
        this.params = ctx.params;
    }
}
