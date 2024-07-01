package rocks.blackblock.bib.bv.operator;

import rocks.blackblock.bib.bv.value.BvElement;
import rocks.blackblock.bib.monitor.GlitchGuru;
import rocks.blackblock.bib.util.BibLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Operators for working with Bv elements
 *
 * @author   Jelle De Loecker <jelle@elevenways.be>
 * @since    0.1.0
 */
@SuppressWarnings({
        // Ignore unused warnings: this is a library after all
        "unused",

        // Ignore warnings of using raw types
        "rawtypes",

        // Ignore unchecked typecast warnings
        "unchecked"
})
public class BvOperator<U extends BvElement> {

    public final Class<? extends BvElement> target_class = null;
    public final String name;
    public final Type type;
    public final Arity arity;
    private UnaryOperatorExecutor<U> unary_executor = null;
    private BinaryOperatorExecutor<U> binary_executor = null;
    private TernaryOperatorExecutor<U> ternary_executor = null;
    
    /**
     * Instantiate the operator without an executor
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public BvOperator(Class<U> target_class, String name, Type type, Arity arity) {
        this.name = name;
        this.type = type;
        this.arity = arity;

        Map<String, BvOperator> operators = BvOperators.REGISTRY.computeIfAbsent(target_class, flowValue -> new HashMap<>());
        operators.put(name, this);
    }

    /**
     * Instantiate the operator as a unary operator
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public BvOperator(Class<U> target_class, String name, Type type, UnaryOperatorExecutor<U> executor) {
        this(target_class, name, type, Arity.UNARY);
        this.unary_executor = executor;
    }

    /**
     * Instantiate the operator as a binary operator
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public BvOperator(Class<U> target_class, String name, Type type, BinaryOperatorExecutor<U> executor) {
        this(target_class, name, type, Arity.BINARY);
        this.binary_executor = executor;
    }

    /**
     * Instantiate the operator as a ternary operator
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    public BvOperator(Class<U> target_class, String name, Type type, TernaryOperatorExecutor<U> executor) {
        this(target_class, name, type, Arity.BINARY);
        this.ternary_executor = executor;
    }

    /**
     * Actually execute this unary operator
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @return   True or false if it was operator on, or null if it didn't apply.
     */
    public Boolean execute(U single_value) {

        try {
            if (this.arity == Arity.UNARY) {
                if (this.unary_executor != null) {
                    return this.unary_executor.execute(single_value);
                } else {
                    return single_value.executeCustomUnaryOperator(this);
                }
            }
        } catch (Exception e) {
            BibLog.log("Exception while executing unary Operator", this, "on", single_value);
            GlitchGuru.registerThrowable(e);
        }

        return null;
    }

    /**
     * Actually execute this binary operator
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @return   True or false if it was operator on, or null if it didn't apply.
     */
    public Boolean execute(U left, BvElement right) {

        try {
            if (this.arity == Arity.BINARY) {
                if (this.binary_executor != null) {
                    return this.binary_executor.execute(left, right);
                } else {
                    return left.executeCustomBinaryOperator(this, right);
                }
            }
        } catch (Exception e) {
            BibLog.log("Exception while executing binary Operator", this, "on", left, right);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actually execute this ternary operator
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @return   True or false if it was operator on, or null if it didn't apply.
     */
    public Boolean execute(U left, BvElement mid, BvElement right) {

        try {
            if (this.arity == Arity.TERNARY) {
                if (this.ternary_executor != null) {
                    return this.ternary_executor.execute(left, mid, right);
                } else {
                    return left.executeCustomTernaryOperator(this, mid, right);
                }
            }
        } catch (Exception e) {
            BibLog.log("Exception while executing ternary Operator", this, "on", left, mid, right);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return a string representation of this object.
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    public String toString() {
        return "BvOperator{\"" + this.name + "\", type=" + this.type + ", arity=" + this.arity + "}";
    }

    /**
     * The unary operator executor:
     * takes in only 1 value
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @FunctionalInterface
    public interface UnaryOperatorExecutor<U> {
        Boolean execute(U value);
    }

    /**
     * The binary operator executor:
     * takes in 2 values
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @FunctionalInterface
    public interface BinaryOperatorExecutor<U> {
        Boolean execute(U left, BvElement right);
    }

    /**
     * The ternary operator executor:
     * takes in 3 values
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.1.0
     */
    @FunctionalInterface
    public interface TernaryOperatorExecutor<U> {
        Boolean execute(U left, BvElement mid, BvElement right);
    }

    /**
     * The Operator types
     *
     * @since    0.1.0
     */
    public enum Type {
        ASSIGNMENT,
        LOGICAL
    }

    /**
     * The possible arity types
     *
     * @since    0.1.0
     */
    public enum Arity {
        UNARY,
        BINARY,
        TERNARY
    }
}