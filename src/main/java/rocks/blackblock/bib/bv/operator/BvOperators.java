package rocks.blackblock.bib.bv.operator;

import rocks.blackblock.bib.bv.value.BvElement;
import rocks.blackblock.bib.bv.value.BvList;
import rocks.blackblock.bib.bv.value.BvNumber;
import rocks.blackblock.bib.util.BibLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all the Bv operators
 *
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
public final class BvOperators {

    // Have the default operators registered?
    private static boolean DEFAULT_HAS_REGISTERED = false;

    // All the registered operators
    public static Map<Class<? extends BvElement>, Map<String, BvOperator>> REGISTRY = new HashMap<>();

    /**
     * Register operators
     *
     * @since    0.1.0
     */
    public static void register(Runnable runnable) {

        if (!DEFAULT_HAS_REGISTERED) {
            registerDefaultOperators();
            registerNumericalOperators();
            BvList.registerOperators();
        }

        if (runnable == null) {
            return;
        }

        runnable.run();
    }

    /**
     * Register all the basic Bv operators
     *
     * @since    0.1.0
     */
    protected static void registerDefaultOperators() {

        if (DEFAULT_HAS_REGISTERED) {
            return;
        }

        DEFAULT_HAS_REGISTERED = true;

        // Set a value
        new BvOperator<>(BvElement.class, "set", BvOperator.Type.ASSIGNMENT, (left, right) -> {
            left.setContainedValue(right.getContainedValue());
            return null;
        });

        // The basic is_null check
        new BvOperator<>(BvElement.class, "is_null", BvOperator.Type.LOGICAL, (left) -> left == null || left.getContainedValue() == null);

        // The inverse of is_null
        new BvOperator<>(BvElement.class, "is_not_null", BvOperator.Type.LOGICAL, (left) -> {

            if (left == null) {
                return false;
            }

            BvOperator is_null = left.getOperator("is_null");

            if (is_null == null) {
                return null;
            }

            Boolean result = is_null.execute(left);

            if (result == null) {
                return null;
            }

            return !result;
        });

        // The standard equals operator
        new BvOperator<>(BvElement.class, "equals", BvOperator.Type.LOGICAL, (left, right) -> {

            if (left == right) {
                return true;
            }

            if (left == null || right == null) {
                return false;
            }

            Object left_value = left.getContainedValue();
            Object right_value = right.getContainedValue();

            if (left_value == right_value) {
                return true;
            }

            return false;
        });

        new BvOperator<>(BvElement.class, "not_equals", BvOperator.Type.LOGICAL, (left, right) -> {
            BvOperator equals = left.getOperator("equals");

            if (equals == null) {
                return null;
            }

            Boolean result = equals.execute(left, right);

            if (result == null) {
                return null;
            }

            return !result;
        });

        new BvOperator<>(BvElement.class, "is_truthy", BvOperator.Type.LOGICAL, (left) -> {

            if (left == null) {
                return false;
            }

            Object value = left.getContainedValue();

            if (value == null) {
                return false;
            }

            if (value instanceof Boolean bool) {
                return bool;
            } else if (value instanceof String str) {
                return !str.isBlank();
            } else if (value instanceof Number nr) {
                return nr.doubleValue() != 0.0;
            }

            return true;
        });

        new BvOperator<>(BvElement.class, "is_falsy", BvOperator.Type.LOGICAL, (left) -> {
            BvOperator is_truthy = left.getOperator("is_truthy");

            if (is_truthy == null) {
                return null;
            }

            Boolean result = is_truthy.execute(left);

            if (result == null) {
                return null;
            }

            return !result;
        });
    }

    /**
     * Register all the numerical Bv operators
     *
     * @since    0.1.0
     */
    private static void registerNumericalOperators() {

        // Add to the left value
        new BvOperator<>(BvNumber.class, "add", BvOperator.Type.ASSIGNMENT, (left, right) -> {

            BibLog.log("Should add", right, "to the value of", left);

            Number right_value = BvNumber.getNumberValue(right);

            if (right_value == null) {
                return null;
            }

            left.add(right_value);

            BibLog.log(" -- New number value is:", left.getContainedValue(), left);

            return true;
        });

        // Subtract from the left value
        new BvOperator<>(BvNumber.class, "subtract", BvOperator.Type.ASSIGNMENT, (left, right) -> {

            BibLog.log("Should subtract", right, "from the value of", left);

            Number right_value = BvNumber.getNumberValue(right);

            if (right_value == null) {
                return null;
            }

            left.subtract(right_value);

            return true;
        });

        // Multiply the left value
        new BvOperator<>(BvNumber.class, "multiply", BvOperator.Type.ASSIGNMENT, (left, right) -> {

            Number right_value = BvNumber.getNumberValue(right);

            if (right_value == null) {
                return null;
            }

            left.multiply(right_value);

            return true;
        });

        // Divide the left value
        new BvOperator<>(BvNumber.class, "divide", BvOperator.Type.ASSIGNMENT, (left, right) -> {

            Number right_value = BvNumber.getNumberValue(right);

            if (right_value == null) {
                return null;
            }

            left.divide(right_value);

            return true;
        });

        // Module the left value
        new BvOperator<>(BvNumber.class, "modulo", BvOperator.Type.ASSIGNMENT, (left, right) -> {

            Number right_value = BvNumber.getNumberValue(right);

            if (right_value == null) {
                return null;
            }

            left.modulo(right_value);

            return true;
        });


        new BvOperator<>(BvNumber.class, "gt", BvOperator.Type.LOGICAL, (left, right) -> {

            if (left == null) {
                return false;
            }

            if (right instanceof BvNumber right_nr) {
                return left.getDoubleValue() > right_nr.getDoubleValue();
            }

            return false;
        });

        new BvOperator<>(BvNumber.class, "gte", BvOperator.Type.LOGICAL, (left, right) -> {

            if (left == null) {
                return false;
            }

            if (right instanceof BvNumber right_nr) {
                return left.getDoubleValue() >= right_nr.getDoubleValue();
            }

            return false;
        });

        new BvOperator<>(BvNumber.class, "lt", BvOperator.Type.LOGICAL, (left, right) -> {

            if (left == null) {
                return false;
            }

            if (right instanceof BvNumber right_nr) {
                return left.getDoubleValue() < right_nr.getDoubleValue();
            }

            return false;
        });

        new BvOperator<>(BvNumber.class, "lte", BvOperator.Type.LOGICAL, (left, right) -> {

            if (left == null) {
                return false;
            }

            if (right instanceof BvNumber right_nr) {
                return left.getDoubleValue() <= right_nr.getDoubleValue();
            }

            return false;
        });
    }

    /**
     * Get all the operators for the given class
     *
     * @since    0.1.0
     *
     * @param    constructor   The BvElement class to register the operator for
     */
    public static List<BvOperator> getOperators(Class<? extends BvElement> constructor) {
        return getOperators(constructor, null, null);
    }

    /**
     * Get an operator by name
     *
     * @since    0.1.0
     *
     * @param    constructor      The BvElement class to get the operator from
     * @param    operator_name    The name of the operator
     */
    public static BvOperator<? extends BvElement> getOperator(Class<? extends BvElement> constructor, String operator_name) {

        if (constructor == null || operator_name == null || operator_name.isBlank()) {
            BibLog.log("Invalid operator name", operator_name);
            return null;
        }

        Map<String, BvOperator> operators = getUnsafeOperators(constructor);

        // See if this special class has the wanted operator
        if (operators.containsKey(operator_name)) {
            return operators.get(operator_name);
        }

        // If not, check in the BvElement class
        if (constructor != BvElement.class) {
            operators = getUnsafeOperators(BvElement.class);
            return operators.get(operator_name);
        }

        return null;
    }

    /**
     * Get unsafe map of operators
     *
     * @since    0.1.0
     *
     * @param    constructor   The BvElement class to register the operator for
     */
    private static Map<String, BvOperator> getUnsafeOperators(Class<? extends BvElement> constructor) {
        Map<String, BvOperator> operators = REGISTRY.computeIfAbsent(constructor, flowValue -> new HashMap<>());
        return operators;
    }

    /**
     * Get all the operators for the given class
     * that match the filters
     *
     * @since    0.1.0
     *
     * @param    constructor   The BvElement class to register the operator for
     * @param    type          The operator type filter
     * @param    arity         The arity filter
     */
    public static List<BvOperator> getOperators(Class<? extends BvElement> constructor, BvOperator.Type type, BvOperator.Arity arity) {

        BibLog.log("Getting Operators for constructor", constructor);
        BibLog.log(" -- Limiting to:", type, arity);

        Map<String, BvOperator> operators = getUnsafeOperators(constructor);
        List<BvOperator> result = new ArrayList<>();

        // Iterate over all the operators
        for (BvOperator operator : operators.values()) {

            BibLog.log(" -- Checking operator", operator);

            if (type != null && operator.type != type) {
                continue;
            }

            if (arity != null && operator.arity != arity) {
                continue;
            }

            result.add(operator);
        }

        return result;
    }
    
}
