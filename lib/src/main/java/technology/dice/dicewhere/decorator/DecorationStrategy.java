/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

/**
 * Specified how the decorator treats the fetched lines from the DB source.
 * ANY: decoration is applied if at least one DB sources contains the range
 * ALL: all databases must contain the range for it to be decorated
 * MAJORITY: more than half of the databases should contain the range for it to be decorated
 */
public enum DecorationStrategy {
	ANY, ALL, MAJORITY
}
