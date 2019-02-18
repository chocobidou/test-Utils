package dev.choco.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class AutoTestPojoUtil {

	private Map<Class,Object> mapValueDefault;
	
	private Object tested;
	
	public AutoTestPojoUtil() {
				
		mapValueDefault = new HashMap<Class, Object>();
		
		mapValueDefault.put(boolean.class, new Boolean(true));
        mapValueDefault.put(byte.class, new Byte((byte) 0));
        mapValueDefault.put(char.class, new Character('0'));
        mapValueDefault.put(short.class, new Short((short) 0));
        mapValueDefault.put(int.class, new Integer(0));
        mapValueDefault.put(long.class, new Long(0));
        mapValueDefault.put(float.class, new Float(0f));
        mapValueDefault.put(double.class, new Double(0d));
        mapValueDefault.put(List.class, new ArrayList());
        mapValueDefault.put(BigDecimal.class, BigDecimal.valueOf(0d));
        mapValueDefault.put(String.class, "vide");
		
	}
	
	public boolean testPojo( Class classPojo ) throws Exception {
		
		boolean ret = true;
		
		tested = classPojo.newInstance(); 
				
		Arrays.asList(tested.getClass().getDeclaredConstructors()).stream().forEach(p -> testConstructeur(p));
		
		tested = classPojo.newInstance(); 
		
		//setter
		Arrays.asList(tested.getClass().getDeclaredFields()).stream().filter(p -> !p.isSynthetic()).forEach(p -> alimField(p));
		
		//tested.getClass().getConstructors()[1].getParameters()
		
		// getter
		ret &= Arrays.asList(tested.getClass().getDeclaredFields()).stream().filter(p -> !p.isSynthetic()).map(p -> testField(p)).filter(p -> p.booleanValue() == false).count() == 0;
		
		//hashcode couverture
		 
		
		tested.hashCode();
		
		
		//equals couverture tres partielle
		Object tested2 = classPojo.newInstance();
		
		tested.equals(tested2);
		
		return ret;
		
	}

	private Method getGetter(final Field field) {
		Class<?> theClass = field.getDeclaringClass();

		Method theMethod = null;
		try {
			theMethod = theClass.getMethod("get" + StringUtils.capitalize(field.getName()));
		} catch (NoSuchMethodException e) {
			try {
				theMethod = theClass.getMethod("is" + StringUtils.capitalize(field.getName()));
			} catch (NoSuchMethodException e1) {
				return null;
			}
		}

		if (theMethod != null && !theMethod.getReturnType().equals(field.getType())) {
			theMethod = null;
		}
		return theMethod;
	}

	private Method getSetter(final Field field) {

		 Class <?> theClass = field.getDeclaringClass();
	        try {
	            return theClass.getMethod("set" + StringUtils.capitalize(field.getName()),
	                    field.getType());
	        } catch (NoSuchMethodException e) {
	            return null;
	        }
	}
	
	private Object getArgsFromTypedField(Class myClazz) {
		
        return mapValueDefault.get(myClazz);
        
	}
	
	private void alimField(Field field) {
		
		try {
				getSetter(field).invoke(tested, getArgsFromTypedField(field.getType()));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private boolean testField(Field field) {
		
		boolean ret = false;
		
		Object retInvoke = null;
		
		try {
			retInvoke = getGetter(field).invoke(tested);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if(retInvoke !=null) {
			ret = (retInvoke.equals(mapValueDefault.get(field.getType())));
		}
		else if( !mapValueDefault.containsKey(field.getType()) ){
			ret = true;
		}
		
		return ret;
		
		
	}
	
	private boolean testConstructeur(Constructor constructor) {
		
		Object obj;
		boolean ret = false;
		
		
		if(constructor.getParameterCount() == 0) {
			try {
				obj = constructor.newInstance();
				
				ret = obj.getClass().isInstance(constructor.getClass());
				
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			
			List<Object> tabParam = new ArrayList<>();
			
			List<Parameter> paramConstruct = Arrays.asList(constructor.getParameters());
			
			paramConstruct.stream().forEach( p -> tabParam.add(getArgsFromTypedField(p.getType())));
			
			List<String> nomParam = paramConstruct.stream().map(p -> p.getName()).collect(Collectors.toList());
			
			try {
				obj = constructor.newInstance(tabParam.toArray());
				
				ret =	Arrays.asList(obj.getClass().getDeclaredFields()).stream().filter(p -> !p.isSynthetic()).filter(p -> nomParam.contains(p.getName())).map(p -> testField(p)).filter(p -> p.booleanValue() == false).count() ==0;
				
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret;
		
	}
	
	

}
