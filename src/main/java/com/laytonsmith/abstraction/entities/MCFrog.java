package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.annotations.MEnum;

public interface MCFrog extends MCLivingEntity {

	@MEnum("com.commandhelper.FrogType")
	enum MCFrogType {
		TEMPERATE, WARM, COLD
	}

	MCFrogType getFrogType();
	void setFrogType(MCFrogType type);
	MCEntity getTongueTarget();
	void setTongueTarget(MCEntity target);

}
