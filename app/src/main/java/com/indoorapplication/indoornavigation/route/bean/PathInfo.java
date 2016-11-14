package com.indoorapplication.indoornavigation.route.bean;

import java.util.List;

/**
 * Created by liyuanqing on 2016/11/2.
 */

public class PathInfo {


    /**
     * route : [{"path":{"naviInfoList":[{"segDistance":124,"fn":"F1","action":"0x06","geometry":[{"x":116.3907,"y":39.993217},{"x":116.39072,"y":39.992897},{"x":116.390724,"y":39.992844},{"x":116.39064,"y":39.99284},{"x":116.39064,"y":39.99284},{"x":116.39059,"y":39.99284},{"x":116.39056,"y":39.992836},{"x":116.3905,"y":39.992836},{"x":116.390495,"y":39.992695},{"x":116.39042,"y":39.99269},{"x":116.39027,"y":39.992687},{"x":116.390274,"y":39.992428}],"floor":"1","buildingId":"B000A80ZU6"}],"start":{"dsp":"奥林匹克比赛大厅","x":116.3907,"y":39.993217,"id":"ZH0000300210100152","time":"四月至十月:00009:00-19:00|十一月至三月09:00-17:30","id_ty":1},"end":{"dsp":"嬉水乐园","x":116.390274,"y":39.992428,"id":"ZH0000300210100024","time":"四月至十月:00009:00-19:00|十一月至三月09:00-17:30","id_ty":1}},"distance":124,"building":"水立方","status":"0","buildingId":"B000A80ZU6"}]
     */
    private List<RouteEntity> route;

    public void setRoute(List<RouteEntity> route) {
        this.route = route;
    }

    public List<RouteEntity> getRoute() {
        return route;
    }

    public class RouteEntity {
        /**
         * path : {"naviInfoList":[{"segDistance":124,"fn":"F1","action":"0x06","geometry":[{"x":116.3907,"y":39.993217},{"x":116.39072,"y":39.992897},{"x":116.390724,"y":39.992844},{"x":116.39064,"y":39.99284},{"x":116.39064,"y":39.99284},{"x":116.39059,"y":39.99284},{"x":116.39056,"y":39.992836},{"x":116.3905,"y":39.992836},{"x":116.390495,"y":39.992695},{"x":116.39042,"y":39.99269},{"x":116.39027,"y":39.992687},{"x":116.390274,"y":39.992428}],"floor":"1","buildingId":"B000A80ZU6"}],"start":{"dsp":"奥林匹克比赛大厅","x":116.3907,"y":39.993217,"id":"ZH0000300210100152","time":"四月至十月:00009:00-19:00|十一月至三月09:00-17:30","id_ty":1},"end":{"dsp":"嬉水乐园","x":116.390274,"y":39.992428,"id":"ZH0000300210100024","time":"四月至十月:00009:00-19:00|十一月至三月09:00-17:30","id_ty":1}}
         * distance : 124
         * building : 水立方
         * status : 0
         * buildingId : B000A80ZU6
         */
        private PathEntity path;
        private int distance;
        private String building;
        private String status;
        private String buildingId;

        public void setPath(PathEntity path) {
            this.path = path;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public void setBuilding(String building) {
            this.building = building;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setBuildingId(String buildingId) {
            this.buildingId = buildingId;
        }

        public PathEntity getPath() {
            return path;
        }

        public int getDistance() {
            return distance;
        }

        public String getBuilding() {
            return building;
        }

        public String getStatus() {
            return status;
        }

        public String getBuildingId() {
            return buildingId;
        }

        public class PathEntity {
            /**
             * naviInfoList : [{"segDistance":124,"fn":"F1","action":"0x06","geometry":[{"x":116.3907,"y":39.993217},{"x":116.39072,"y":39.992897},{"x":116.390724,"y":39.992844},{"x":116.39064,"y":39.99284},{"x":116.39064,"y":39.99284},{"x":116.39059,"y":39.99284},{"x":116.39056,"y":39.992836},{"x":116.3905,"y":39.992836},{"x":116.390495,"y":39.992695},{"x":116.39042,"y":39.99269},{"x":116.39027,"y":39.992687},{"x":116.390274,"y":39.992428}],"floor":"1","buildingId":"B000A80ZU6"}]
             * start : {"dsp":"奥林匹克比赛大厅","x":116.3907,"y":39.993217,"id":"ZH0000300210100152","time":"四月至十月:00009:00-19:00|十一月至三月09:00-17:30","id_ty":1}
             * end : {"dsp":"嬉水乐园","x":116.390274,"y":39.992428,"id":"ZH0000300210100024","time":"四月至十月:00009:00-19:00|十一月至三月09:00-17:30","id_ty":1}
             */
            private List<NaviInfoListEntity> naviInfoList;
            private StartEntity start;
            private EndEntity end;

            public void setNaviInfoList(List<NaviInfoListEntity> naviInfoList) {
                this.naviInfoList = naviInfoList;
            }

            public void setStart(StartEntity start) {
                this.start = start;
            }

            public void setEnd(EndEntity end) {
                this.end = end;
            }

            public List<NaviInfoListEntity> getNaviInfoList() {
                return naviInfoList;
            }

            public StartEntity getStart() {
                return start;
            }

            public EndEntity getEnd() {
                return end;
            }

            public class NaviInfoListEntity {
                /**
                 * segDistance : 124
                 * fn : F1
                 * action : 0x06
                 * geometry : [{"x":116.3907,"y":39.993217},{"x":116.39072,"y":39.992897},{"x":116.390724,"y":39.992844},{"x":116.39064,"y":39.99284},{"x":116.39064,"y":39.99284},{"x":116.39059,"y":39.99284},{"x":116.39056,"y":39.992836},{"x":116.3905,"y":39.992836},{"x":116.390495,"y":39.992695},{"x":116.39042,"y":39.99269},{"x":116.39027,"y":39.992687},{"x":116.390274,"y":39.992428}]
                 * floor : 1
                 * buildingId : B000A80ZU6
                 */
                private int segDistance;
                private String fn;
                private String action;
                private List<GeometryEntity> geometry;
                private String floor;
                private String buildingId;

                public void setSegDistance(int segDistance) {
                    this.segDistance = segDistance;
                }

                public void setFn(String fn) {
                    this.fn = fn;
                }

                public void setAction(String action) {
                    this.action = action;
                }

                public void setGeometry(List<GeometryEntity> geometry) {
                    this.geometry = geometry;
                }

                public void setFloor(String floor) {
                    this.floor = floor;
                }

                public void setBuildingId(String buildingId) {
                    this.buildingId = buildingId;
                }

                public int getSegDistance() {
                    return segDistance;
                }

                public String getFn() {
                    return fn;
                }

                public String getAction() {
                    return action;
                }

                public List<GeometryEntity> getGeometry() {
                    return geometry;
                }

                public String getFloor() {
                    return floor;
                }

                public String getBuildingId() {
                    return buildingId;
                }

//                public class GeometryEntity {
//                    /**
//                     * x : 116.3907
//                     * y : 39.993217
//                     */
//                    private double x;
//                    private double y;
//
//                    public void setX(double x) {
//                        this.x = x;
//                    }
//
//                    public void setY(double y) {
//                        this.y = y;
//                    }
//
//                    public double getX() {
//                        return x;
//                    }
//
//                    public double getY() {
//                        return y;
//                    }
//                }
            }

            public class StartEntity {
                /**
                 * dsp : 奥林匹克比赛大厅
                 * x : 116.3907
                 * y : 39.993217
                 * id : ZH0000300210100152
                 * time : 四月至十月:00009:00-19:00|十一月至三月09:00-17:30
                 * id_ty : 1
                 */
                private String dsp;
                private double x;
                private double y;
                private String id;
                private String time;
                private int id_ty;

                public void setDsp(String dsp) {
                    this.dsp = dsp;
                }

                public void setX(double x) {
                    this.x = x;
                }

                public void setY(double y) {
                    this.y = y;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public void setTime(String time) {
                    this.time = time;
                }

                public void setId_ty(int id_ty) {
                    this.id_ty = id_ty;
                }

                public String getDsp() {
                    return dsp;
                }

                public double getX() {
                    return x;
                }

                public double getY() {
                    return y;
                }

                public String getId() {
                    return id;
                }

                public String getTime() {
                    return time;
                }

                public int getId_ty() {
                    return id_ty;
                }
            }

            public class EndEntity {
                /**
                 * dsp : 嬉水乐园
                 * x : 116.390274
                 * y : 39.992428
                 * id : ZH0000300210100024
                 * time : 四月至十月:00009:00-19:00|十一月至三月09:00-17:30
                 * id_ty : 1
                 */
                private String dsp;
                private double x;
                private double y;
                private String id;
                private String time;
                private int id_ty;

                public void setDsp(String dsp) {
                    this.dsp = dsp;
                }

                public void setX(double x) {
                    this.x = x;
                }

                public void setY(double y) {
                    this.y = y;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public void setTime(String time) {
                    this.time = time;
                }

                public void setId_ty(int id_ty) {
                    this.id_ty = id_ty;
                }

                public String getDsp() {
                    return dsp;
                }

                public double getX() {
                    return x;
                }

                public double getY() {
                    return y;
                }

                public String getId() {
                    return id;
                }

                public String getTime() {
                    return time;
                }

                public int getId_ty() {
                    return id_ty;
                }
            }
        }
    }
}
