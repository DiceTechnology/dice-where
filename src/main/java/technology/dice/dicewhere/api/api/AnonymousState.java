package technology.dice.dicewhere.api.api;

import technology.dice.dicewhere.lineprocessing.serializers.protobuf.AnonymousStateProtoOuterClass;

import java.util.Arrays;

/**
 * Created by stan on 14/09/2018
 */

public enum AnonymousState {
	NOT_SPECIFIED, NOT_ANONYMOUS, IS_ANONYMOUS, IS_ANONYMOUS_VPN;

	public static AnonymousState fromAnonymousStateProto(AnonymousStateProtoOuterClass.AnonymousStateProto protoValue) {
		return Arrays.stream(AnonymousState.values()).filter(a -> a.name().equals(protoValue.name())).findFirst().orElse(NOT_SPECIFIED);
	}
}
