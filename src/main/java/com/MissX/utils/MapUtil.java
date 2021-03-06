package com.MissX.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.MissX.utils.Enum.EXCEPTION;
import com.MissX.utils.Enum.PRINT;

public class MapUtil {
	
	/**
	 * 将回调返回的XML字符串转换为Map(仅可在微信订单返回数据时使用)
	 * @param str 微信返回的XML格式字符串
	 * @param checkErroe 是否需要检测错误信息
	 * @return XML转换的map
	 */
	public static TreeMap<String,String> XMLStringToMap(String str,EXCEPTION exception,PRINT print) {
		Document document;
		try {
			document = DocumentHelper.parseText(str);
			@SuppressWarnings("unchecked")
			List<Element> elements = document.getRootElement().elements();
			TreeMap<String,String> resultMap = new TreeMap<String,String>();
			for (Element element : elements) {
				if(EXCEPTION.check(exception)) {
					if("result_code".equals(element.getName())) {
						if((element.getText().indexOf("FAIL")!=-1)) {
							for (Element err : elements) {
								if("err_code".equals(err.getName())) {
									throw new RuntimeException(err.getText());
								}
							}
						}
					}
					if("err_code_des".equals(element.getName())) {
						throw new RuntimeException(element.getText());
					}
				}
				if(PRINT.check(print)) {
					System.out.println(element.getName()+"："+element.getText());
				}
				resultMap.put(element.getName(), element.getText());
			}
			return resultMap;
		} catch (DocumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * 将map转换为微信所需的XML格式字符串(仅可在微信订单下单时使用)
	 * @param map 需包含要提交的各项参数
	 * @return	返回值为微信需要的xml格式
	 */
	public static String MapToXMLString(Map<String, String> map) {
		Set<String> keySet = map.keySet();
		String xml = "<xml>";
		String ss = "";
		for (String string : keySet) {
			String str ="";
			if("sign".equals(string)) {
				ss = "<"+string+">"+map.get(string)+"</"+string+">";
			} else {
				str = "<"+string+"><![CDATA["+map.get(string)+"]]></"+string+">";
			}
			xml+=str;
		}
		xml+=ss;
		xml+="</xml>";
		return xml;
	}
	
	/**
	 * 将实体类转换为map
	 * @param model
	 * @return
	 * @throws ModelToMapFormatException 
	 */
	public static <T> Map<String, String> ModelToMap(T model) throws ModelToMapFormatException {
		try {
			Map<String,String> map = new HashMap<String,String>();
				Method[] methods = model.getClass().getMethods();	//获取所有自定义的方法
				for (Method method : methods) {	//遍历所有自定义方法
					if(method.getName().startsWith("get")) {	//判断当前方法是否为get方法
						Object result = method.invoke(model);	//执行当前get方法并获取返回值
						if(null==result) 
							continue;
						map.put(method.getName().replace("get", ""), result.toString());
					}
				}
			return map;
		}catch(Exception e){
			throw new ModelToMapFormatException();
		}
	}
	/**
	 * 实体类转换map异常
	 * 2019年2月14日 下午2:25:47
	 * @author H
	 * TODO
	 * Admin
	 */
	private static class ModelToMapFormatException extends Exception{
		private static final long serialVersionUID = 1L;
	}
}