package ca.applin.jim.ast;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.AstVisitor.Visited;
import java.util.Objects;

public interface Ast extends AstVisitor.Visited {

    @Override
    default void visit(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    record Atom(String value, int hash) {
        public Atom(String value) {
            this(value, Objects.hash(value));
        }

        @Override
        public String toString() {
            return "Atom['" + value + "']";
        }

        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (! (obj instanceof Atom other)) return false;
            return other.hash == this.hash;
        }
        public int hashCode() {
            return hash;
        }
    }

    default Ast unpack() {
        return this;
    }

    // linked list node
    class CodeBlock implements Ast {
        public Ast elem;
        public CodeBlock next;

        public CodeBlock(Ast elem, CodeBlock next) {
            this.elem = elem;
            this.next = next;
        }

        public CodeBlock(Ast elem) {
            this(elem, null);
        }

        public void append(Ast elem) {
           if (next == null) {
               next = new CodeBlock(elem, null);
           } else {
               next.append(elem);
           }
        }

        @Override
        public String toString() {
            return next == null
                    ? "Block[elem=%s]".formatted(elem.toString())
                    : "Block[elem=%s, next=%s]".formatted(elem.toString(), next.toString());
        }

        @Override
        public void visit(AstVisitor astVisitor) {
            astVisitor.visit(this);
        }
    }

    static Maybe<CodeBlock> append(Maybe<CodeBlock> mBlock, Maybe<Ast> mNext) {
        if (mBlock.isNothing()) {
            if (mNext instanceof Just<Ast> jNext) {
                return just(new CodeBlock(jNext.elem()));
            }
            return nothing();
        }
        if (mNext.isNothing()) {
            return mBlock;
        }
        CodeBlock cb = ((Just<CodeBlock>) mBlock).elem();
        Ast next = ((Just<Ast>) mNext).elem();
        cb.append(next);
        return mBlock;
    }

}