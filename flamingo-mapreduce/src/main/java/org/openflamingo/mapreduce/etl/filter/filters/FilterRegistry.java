package org.openflamingo.mapreduce.etl.filter.filters;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper와 Reducer에서 지정한 필터링 조건에 맞는 필터링을 수행하기 위해서
 * 필요한 필터를 등록하여 관리하는 레지스트리.
 * 신규 필터를 구성하는 경우 반드시 이 레지스트리에 등록해야 한다.
 *
 * @author Edward KIM
 * @author Seo Ji Hye
 * @since 0.1
 */
public class FilterRegistry {

	/**
	 * 필터의 이름과 필터의 구현체를 매핑하는 필터맵.
	 */
	private static Map<String, Filter> filterMap = new HashMap<String, Filter>();

	static {
		filterMap.put("EMPTY", new EmptyColumnFilter());
		filterMap.put("NEMPTY", new NotEmptyColumnFilter());
		filterMap.put("EQSTR", new EqualStringFilter());
		filterMap.put("NEQSTR", new NotEqualStringFilter());
		filterMap.put("EQNUM", new EqualNumberFilter());
		filterMap.put("NEQNUM", new NotEqualNumberFilter());
		filterMap.put("LT", new LessThanFilter());
		filterMap.put("LTE", new LessThanEqualFilter());
		filterMap.put("GT", new GreaterThanFilter());
		filterMap.put("GTE", new GreaterThanEqualFilter());
		filterMap.put("STARTWITH", new StartWithFilter());
		filterMap.put("ENDWITH", new EndWithFilter());
	}

	/**
	 * 지정한 이름읠 필터를 반환한다.
	 *
	 * @param name 필터명
	 * @return 필터 구현체
	 */
	public static Filter getFilter(String name) {
		return filterMap.get(name.toUpperCase());
	}
}
