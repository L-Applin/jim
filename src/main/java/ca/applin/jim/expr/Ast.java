package ca.applin.jim.expr;

import java.util.Objects;

public interface Ast {

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

    // linked list
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
    }

}