package com.comp90018.uninooks.service.study_space;

import com.comp90018.uninooks.models.location.Location;
import com.comp90018.uninooks.models.location.study_space.StudySpace;
import com.comp90018.uninooks.models.review.ReviewType;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public interface StudySpaceService {

//    Location getStudySpaceLocation(int favouriteId, ReviewType type) throws Exception;

    ArrayList<StudySpace> getClosestStudySpaces(LatLng location, int size) throws Exception;
}