package org.andrewberman.ui.tween;

import java.lang.reflect.Field;

public class PropertyTween extends Tween
{
	Object o;
	Field field;
	Class fieldClass;
	String prop;
	
	public PropertyTween(Object object, String propertyName, TweenFunction function, int type,
			float start, float end, float duration)
	{
		super(null, function, type, start, end, duration);
		
		this.o = object;
		this.prop = propertyName;
		try
		{
			field = o.getClass().getField(prop);
//			field.setAccessible(true);
			fieldClass = field.getType();
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
	}

	public float update()
	{
		float value = super.update();
		if (field == null) return value;
		try
		{
			if (fieldClass == float.class)
				field.setFloat(o, value);
			else if (fieldClass == int.class)
				field.setInt(o, (int)value);
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return value;
	}
}
