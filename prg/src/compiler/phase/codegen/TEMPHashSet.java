package compiler.phase.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import compiler.data.imc.TEMP;

public class TEMPHashSet extends HashSet<TEMP> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TEMPHashSet(Set<TEMP> out) {
		super(out);
	}

	public TEMPHashSet() {
		super();
	}

	@Override
	public boolean addAll(Collection<? extends TEMP> c) {
		boolean hasChanged = false;
		for(TEMP t : c) {
			if(!this.contains(t)) {
				this.add(t);
				hasChanged = true;
			}
		}
		return hasChanged;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean hasChanged = false;
		for(Object o : c) {
			TEMP t = (TEMP)o;
			ArrayList<TEMP> toRemove = new ArrayList<TEMP>();
			for (TEMP temp : this) {
				if(t.name == temp.name) {
					toRemove.add(temp);
					hasChanged = true;
				}
			}
			super.removeAll(toRemove);
		}
		return hasChanged;
	}
	
	@Override
	public boolean equals(Object o) {
		TEMPHashSet set = (TEMPHashSet)o;
		if(this.size() != set.size()) {
			return false;
		}
		for(TEMP t : set) {
			if(!this.contains(t)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean contains(Object o) {
		TEMP temp = (TEMP)o;
		for(TEMP t : this) {
			if(t.name == temp.name) {
				return true;
			}
		}
		return false;
	}
	
}
