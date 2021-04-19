package net.lotushq.languages;

import net.lotushq.languages.annotation.FormatPreset;
import net.lotushq.languages.annotation.MessageInfo;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An InvocationHandler that uses the proxy to retrieve the {@link MessageInfo} annotation from the called method.
 * The annotation then provides the path to the requested message in the configuration, as well as basic formatting.
 * Finally, the objects passed as parameters to the called method are used to format the raw message.
 */
public final class MessageRetrievalHandler implements InvocationHandler {

    private final YamlConfiguration yamlConfiguration;
    private MessageProvider proxy;

    MessageRetrievalHandler(YamlConfiguration configuration) {
        this.yamlConfiguration = configuration;
    }

    MessageProvider getProxy() {
        return this.proxy;
    }

    void setProxy(MessageProvider proxy) {
        this.proxy = proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // Required to handle default methods common to all MessageProviders. such as prefix() and unknown()
        if (method.isDefault()) {
            try {
                return handleDefault(proxy, method, args);
            } catch (Throwable e) {
                // uhm yeah that's awkward
                return "";
            }
        }

        // Retrieve the annotation that contains the message's configuration info
        MessageInfo info = method.getAnnotation(MessageInfo.class);
        Object[] remainingArgs = args;
        String path = info.path();

        // If the path contains unresolved replacements, consume an argument for each replacement.
        if (info.variablePath()) {
            int pathReplacements = Math.min(StringUtils.countMatches(path, "%s"), args.length);
            path = String.format(path, args);
            remainingArgs = Arrays.copyOfRange(args, pathReplacements, args.length);
        }

        // Retrieve the prefix from the config if the FormatPreset calls for it.
        FormatPreset formatPreset = info.format();
        String prefix = formatPreset == FormatPreset.PLAIN ? null : ((MessageProvider) proxy).prefix();

        // Apply plain formatting to all elements of the list and return
        if (List.class.isAssignableFrom(method.getReturnType())) {
            return yamlConfiguration.getStringList(path).stream()
                    .map(string -> formatPreset.format(prefix, string))
                    .collect(Collectors.toList());
        }

        // Retrieve the raw message from the configuration file.
        String rawMessage = yamlConfiguration.getString(path);

        // Append the prefix to the raw message and resolve replacements by formatting the string.
        return String.format(formatPreset.format(prefix, rawMessage), remainingArgs);
    }

    private Object handleDefault(Object proxy, Method method, Object[] args) throws Throwable {
        MethodHandles.Lookup lookup =
                MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());

        MethodHandle handle;
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());

        if (Modifier.isStatic(method.getModifiers())) {
            handle = lookup.findStatic(method.getDeclaringClass(), method.getName(), methodType);
        } else {
            handle = lookup.findSpecial(method.getDeclaringClass(), method.getName(), methodType, method.getDeclaringClass());
        }

        return handle.bindTo(proxy).invokeWithArguments(args);
    }

}
