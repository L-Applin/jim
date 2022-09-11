package ca.applin.jim.ast;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.Location;
import java.util.List;
import java.util.Objects;

public interface Decl extends Ast {

    boolean isConst();
    Location location();
    Atom name();

    record TypeDecl(
            Location location,
            Atom name,
            List<String> generics,
            Type type
    ) implements Decl {
        public boolean isConst() {
            return true;
        }
        public TypeDecl(LexerToken lexerToken, List<String> generics, Type type) {
            this(lexerToken.location(), new Atom(lexerToken.str()), generics, type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TypeDecl typeDecl = (TypeDecl) o;
            if (!Objects.equals(name, typeDecl.name))         return false;
            if (!Objects.equals(generics, typeDecl.generics)) return false;
            return Objects.equals(type, typeDecl.type);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (generics != null ? generics.hashCode() : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }

        @Override
        public void visit(AstVisitor astVisitor) {
            astVisitor.visit(this);
        }
    }

    record VarDecl(
        Location location,
        Atom name,
        boolean isConst,
        Maybe<Type> typeDecl,
        Expr expr
    ) implements Decl {

        public VarDecl(LexerToken lexerToken, boolean isConst, Maybe<Type> typeDecl, Expr expr) {
            this(lexerToken.location(), new Atom(lexerToken.str()), isConst, typeDecl, expr);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VarDecl varDecl = (VarDecl) o;
            if (isConst() != varDecl.isConst())              return false;
            if (!Objects.equals(location, varDecl.location)) return false;
            if (!Objects.equals(name, varDecl.name))         return false;
            if (!Objects.equals(typeDecl, varDecl.typeDecl)) return false;
            return Objects.equals(expr, varDecl.expr);
        }

        @Override
        public int hashCode() {
            int result = location != null ? location.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (isConst() ? 1 : 0);
            result = 31 * result + (typeDecl != null ? typeDecl.hashCode() : 0);
            result = 31 * result + (expr != null ? expr.hashCode() : 0);
            return result;
        }

        @Override
        public void visit(AstVisitor astVisitor) {
            astVisitor.visit(this);
        }
    }

    record VarAssign(
        Location location,
        Atom name,
        Expr expr
    ) implements Decl{
        public boolean isConst() {
            return false;
        }

        @Override
        public void visit(AstVisitor astVisitor) {
            astVisitor.visit(this);
        }
    }

    record FunctionDecl(
        Location location,
        Atom name,
        Maybe<Type> type,
        List<Atom> args,
        Ast body
    ) implements Decl {
        public boolean isConst() { return true; }

        @Override
        public void visit(AstVisitor astVisitor) {
            astVisitor.visit(this);
        }
    }

}
