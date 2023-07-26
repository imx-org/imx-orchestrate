package nl.geostandaarden.imx.orchestrate.model.matchers;

import java.util.HashMap;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.ModelException;
import org.projectnessie.cel.checker.Decls;
import org.projectnessie.cel.tools.Script;
import org.projectnessie.cel.tools.ScriptCreateException;
import org.projectnessie.cel.tools.ScriptException;
import org.projectnessie.cel.tools.ScriptHost;

public final class CelMatcherType implements MatcherType {

  @Override
  public String getName() {
    return "cel";
  }

  @Override
  public Matcher create(Map<String, Object> options) {
    var scriptHost = ScriptHost.newBuilder()
        .build();

    var expr = (String) options.get("expr");
    Script script;

    try {
      script = scriptHost.buildScript(expr)
          .withDeclarations(Decls.newVar("value", Decls.Any))
          .build();
    } catch (ScriptCreateException e) {
      throw new ModelException("Could not parse expression: " + expr, e);
    }

    return value -> {
      var arguments = new HashMap<String, Object>();
      arguments.put("value", value);

      try {
        return script.execute(Boolean.class, arguments);
      } catch (ScriptException e) {
        throw new ModelException("Could not evaluate expression: " + expr, e);
      }
    };
  }
}
