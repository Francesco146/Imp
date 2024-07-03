import org.antlr.v4.runtime.Token;
import value.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class IntImp extends ImpBaseVisitor<Value> {

    private final Conf conf;

    public IntImp(Conf conf) {
        this.conf = conf;
    }

    private ComValue visitCom(ImpParser.ComContext ctx) {
        return (ComValue) visit(ctx);
    }

    private ExpValue<?> visitExp(ImpParser.ExpContext ctx) {
        return (ExpValue<?>) visit(ctx);
    }

    private int visitNatExp(ImpParser.ExpContext ctx) {
        try {
            return ((NatValue) visitExp(ctx)).toJavaValue();
        } catch (ClassCastException e) {
            printError(ctx.start, ctx.getText(), "> Natural expression expected.");
        }

        return 0; // unreachable
    }

    private static void printError(Token ctx, String ctx1, String x) {
        System.err.println("Type mismatch exception!");
        System.err.println("@" + ctx.getLine() + ":" + ctx.getCharPositionInLine());
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>");
        System.err.println(ctx1);
        System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<");
        System.err.println(x);
        System.exit(1);
    }

    private boolean visitBoolExp(ImpParser.ExpContext ctx) {
        try {
            return ((BoolValue) visitExp(ctx)).toJavaValue();
        } catch (ClassCastException e) {
            printError(ctx.start, ctx.getText(), "> Boolean expression expected.");
        }

        return false; // unreachable
    }

    @Override
    public ComValue visitProg(ImpParser.ProgContext ctx) {
        return visitCom(ctx.com());
    }

    @Override
    public ComValue visitIf(ImpParser.IfContext ctx) {
        return visitBoolExp(ctx.exp())
                ? visitCom(ctx.com(0))
                : visitCom(ctx.com(1));
    }

    @Override
    public ComValue visitAssign(ImpParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        ExpValue<?> v = visitExp(ctx.exp());

        conf.update(id, v);

        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitArrayAssignCom(ImpParser.ArrayAssignComContext ctx) {
        String id = ctx.ID().getText();
        int index = visitNatExp(ctx.exp(0));
        ExpValue<?> value = visitExp(ctx.exp(1));

        if (conf.contains(id) && conf.get(id) instanceof ArrayValue array) {
            array.set(index, value);
            conf.update(id, new ArrayValue(array.getValues()));

            return ComValue.INSTANCE;
        }

        if (index == 0) {
            conf.update(id, new ArrayValue(List.of(value)));
            return ComValue.INSTANCE;
        }

        List<ExpValue<?>> values = new ArrayList<>();
        IntStream.range(0, index).forEach(i -> values.add(i, null));

        values.add(value);

        conf.update(id, new ArrayValue(values));

        return ComValue.INSTANCE;
    }


    @Override
    public ComValue visitSkip(ImpParser.SkipContext ctx) {
        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitSeq(ImpParser.SeqContext ctx) {
        visitCom(ctx.com(0));
        return visitCom(ctx.com(1));
    }

    @Override
    public ComValue visitWhile(ImpParser.WhileContext ctx) {
        if (!visitBoolExp(ctx.exp()))
            return ComValue.INSTANCE;

        visitCom(ctx.com());

        return visitWhile(ctx);
    }

    @Override
    public ComValue visitOut(ImpParser.OutContext ctx) {
        ExpValue<?> value = visitExp(ctx.exp());

        if (!(value instanceof StringValue))
            printError(ctx.start, ctx.getText(), "> String expression expected.");

        System.out.println(value);
        return ComValue.INSTANCE;

    }

    @Override
    public StringValue visitToString(ImpParser.ToStringContext ctx) {
        return new StringValue(visit(ctx.exp()).toString());
    }

    @Override
    public NatValue visitNat(ImpParser.NatContext ctx) {
        return new NatValue(Integer.parseInt(ctx.NAT().getText()));
    }

    @Override
    public BoolValue visitBool(ImpParser.BoolContext ctx) {
        return new BoolValue(Boolean.parseBoolean(ctx.BOOL().getText()));
    }

    @Override
    public StringValue visitString(ImpParser.StringContext ctx) {
        String value = ctx.STRING().toString();
        return new StringValue(value.substring(1, value.length() - 1));

    }

    @Override
    public ExpValue<?> visitParExp(ImpParser.ParExpContext ctx) {
        return visitExp(ctx.exp());
    }

    @Override
    public NatValue visitPow(ImpParser.PowContext ctx) {
        int base = visitNatExp(ctx.exp(0));
        int exp = visitNatExp(ctx.exp(1));

        return new NatValue((int) Math.pow(base, exp));
    }

    @Override
    public BoolValue visitNot(ImpParser.NotContext ctx) {
        return new BoolValue(!visitBoolExp(ctx.exp()));
    }

    @Override
    public NatValue visitDivMulMod(ImpParser.DivMulModContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case ImpParser.DIV -> new NatValue(left / right);
            case ImpParser.MUL -> new NatValue(left * right);
            case ImpParser.MOD -> new NatValue(left % right);
            default -> null;
        };
    }

    @Override
    public NatValue visitPlusMinus(ImpParser.PlusMinusContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case ImpParser.PLUS -> new NatValue(left + right);
            case ImpParser.MINUS -> new NatValue(Math.max(left - right, 0));
            default -> null;
        };
    }

    @Override
    public BoolValue visitEqExp(ImpParser.EqExpContext ctx) {
        ExpValue<?> left = visitExp(ctx.exp(0));
        ExpValue<?> right = visitExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case ImpParser.EQQ -> new BoolValue(left.equals(right));
            case ImpParser.NEQ -> new BoolValue(!left.equals(right));
            default -> null; // unreachable
        };
    }

    @Override
    public ExpValue<?> visitId(ImpParser.IdContext ctx) {
        String id = ctx.ID().getText();

        if (!conf.contains(id)) {
            System.err.println("Variable " + id + " used but never instantiated");
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());

            System.exit(1);
        }

        return conf.get(id);
    }

    @Override
    public BoolValue visitCmpExp(ImpParser.CmpExpContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case ImpParser.GEQ -> new BoolValue(left >= right);
            case ImpParser.LEQ -> new BoolValue(left <= right);
            case ImpParser.LT  -> new BoolValue(left < right);
            case ImpParser.GT  -> new BoolValue(left > right);
            default -> null;
        };
    }

    @Override
    public BoolValue visitLogicExp(ImpParser.LogicExpContext ctx) {
        boolean left = visitBoolExp(ctx.exp(0));
        boolean right = visitBoolExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case ImpParser.AND -> new BoolValue(left && right);
            case ImpParser.OR -> new BoolValue(left || right);
            default -> null;
        };
    }

    @Override
    public StringValue visitConcString(ImpParser.ConcStringContext ctx) {
        String value1 = visit(ctx.exp(0)).toString();
        String value2 = visit(ctx.exp(1)).toString();
        return new StringValue(value1 + value2);
    }

    @Override
    public ExpValue<?> visitArrayExp(ImpParser.ArrayExpContext ctx) {
        String id = ctx.ID().getText();
        ExpValue<?> expValue = visitExp(ctx.exp());
        ArrayValue array = (ArrayValue) conf.get(id);


        return array.getValues().get((Integer) expValue.toJavaValue());
    }
}