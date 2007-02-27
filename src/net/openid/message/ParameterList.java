/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;
import java.net.URLDecoder;

/**
 * A list of parameters that are part of an OpenID message. Please note that you can have multiple parameters with
 * the same name.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ParameterList implements Serializable
{
    Map _parameterMap;

    public ParameterList()
    {
        _parameterMap  = new LinkedHashMap();
    }

    public ParameterList(ParameterList that)
    {
        this._parameterMap = new LinkedHashMap(that._parameterMap);
    }

    public ParameterList(Map parameterMap)
    {
        _parameterMap  = new LinkedHashMap();

        Iterator keysIter = parameterMap.keySet().iterator();
        while (keysIter.hasNext())
        {
            String name = (String) keysIter.next();
            Object v = parameterMap.get(name);

            String value;
            if (v instanceof Object[])
            {
                Object[] values = (Object[]) v;
                if (values.length > 1)
                    throw new IllegalArgumentException(
                            "Multiple parameters with the same name: " + values);

                value = values.length > 0 ? (String) values[0] : null;
            }
            else
            {
                value = (String) v;
            }

            set(new Parameter(name, value));
        }
    }

    public void copyOf(ParameterList that)
    {
        this._parameterMap = new LinkedHashMap(that._parameterMap);
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        final ParameterList that = (ParameterList) obj;

        return _parameterMap.equals(that._parameterMap);
    }

    public int hashCode()
    {
        return _parameterMap.hashCode();
    }

    public void set(Parameter parameter)
    {
        _parameterMap.put(parameter.getKey(), parameter);
    }

    public void addParams(ParameterList params)
    {
        Iterator iter = params.getParameters().iterator();

        while (iter.hasNext())
            set((Parameter) iter.next());
    }

    public Parameter getParameter(String name)
    {
        return (Parameter) _parameterMap.get(name);
    }

    public String getParameterValue(String name)
    {
        Parameter param = getParameter(name);

        return param != null ? param.getValue() : null;
    }

    public List getParameters()
    {
        return new ArrayList(_parameterMap.values());
    }

    public void removeParameters(String name)
    {
        _parameterMap.remove(name);
    }

    public boolean hasParameter(String name)
    {
        return _parameterMap.containsKey(name);
    }

    /**
     * Create a parameter list based on a URL encoded HTTP query string.
     */
    public static ParameterList createFromQueryString(String queryString) throws MessageException
    {
        ParameterList parameterList = new ParameterList();

        StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
        while (tokenizer.hasMoreTokens())
        {
            String keyValue = tokenizer.nextToken();
            int posEqual = keyValue.indexOf('=');

            if (posEqual == -1)
                throw new MessageException("Invalid query parameter, = missing: " + keyValue);

            try
            {
                String key   = URLDecoder.decode(keyValue.substring(0, posEqual), "UTF-8");
                String value = URLDecoder.decode(keyValue.substring(posEqual + 1), "UTF-8");

                parameterList.set(new Parameter(key, value));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new MessageException("Cannot URL decode query parameter: " + keyValue, e);
            }
        }

        return parameterList;
    }

    public static ParameterList createFromKeyValueForm(String keyValueForm) throws MessageException
    {
        ParameterList parameterList = new ParameterList();

        StringTokenizer tokenizer = new StringTokenizer(keyValueForm, "\n");
        while (tokenizer.hasMoreTokens())
        {
            String keyValue = tokenizer.nextToken();
            int posColon = keyValue.indexOf(':');

            if (posColon == -1)
                throw new MessageException("Invalid Key-Value form, colon missing: " + keyValue);

            String key   = keyValue.substring(0, posColon);
            String value = keyValue.substring(posColon + 1);

            parameterList.set(new Parameter(key, value));
        }

        return parameterList;
    }

}