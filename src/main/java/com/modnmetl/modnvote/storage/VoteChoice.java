package com.modnmetl.modnvote.storage;

public enum VoteChoice {
    YES, NO;
    public static VoteChoice fromString(String s) {
        if (s == null) return null;
        if (s.equalsIgnoreCase("yes")) return YES;
        if (s.equalsIgnoreCase("no")) return NO;
        return null;
    }
}
