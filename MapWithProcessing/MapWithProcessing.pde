BufferedReader reader;
String line;
int Xleft=30;
int Xright=30;
int Ytop=50;
int Ybottom=50;
int XaxisScale = 2;
int YaxisScale = 2;

int window_width = Xleft+XaxisScale*360+Xright;
int window_height = Ybottom+YaxisScale*360+Ytop;

int Xcenter=Xleft+(int)(((float)XaxisScale/2.0)*360);
//int Ycenter=350;
int Ycenter=Ytop+(int)((YaxisScale/2.0)*360);


void setup() {
  println(Ycenter);
  // Open the file from the createWriter() example
  reader = createReader("positions.txt");  
  size(window_width,window_height);
  drawAxis();
}

void draw() {
  //testOne();
  //testTwo();
  testThree();
  noLoop();
//  if (line == null) {
//    // Stop reading because of an error or file is empty
//    noLoop();  
//  } else {
//    String[] pieces = split(line, ',');
//    int x = int(pieces[0]);
//    int y = int(pieces[1]);
//    point(x, y);
//  }
} 

void drawAxis(){
  //line(30,350,1470,350);
  line(Xleft,Ycenter,window_width-Xright, Ycenter);
  //line(750,50 ,750,650);
  line(Xcenter,Ytop,Xcenter, window_height-Ytop);
  
  //line(30,352,30,348);
  line(Xleft,Ycenter+2,Xleft,Ycenter-2);
  //line(1470,352,1470,348);
  line((XaxisScale*360)+Xright,Ycenter+2,(XaxisScale*360)+Xright,Ycenter-2);
  //line(390,352,390,348);
  line((XaxisScale*(1.0/4.0)*360)+Xright,Ycenter+2,(XaxisScale*(1.0/4.0)*360)+Xright,Ycenter-2);
  //line(1110,352,1110,348);
  line((XaxisScale*(3.0/4.0)*360)+Xright,Ycenter+2,(XaxisScale*(3.0/4.0)*360)+Xright,Ycenter-2);
}

float[] translatePoint(float x, float y){
  float[] result = {((XaxisScale*x)+Xcenter),(Ycenter-(y*YaxisScale))};
  return result;
}

void pixelatedDraw(int[] coords, int latScale, int longScale){
  //reads in indicies and draw in pixels
  for(int i=0;i<coords.length;i++){
   println(Xcenter+(coords[i]*longScale)+","+(Ycenter+(-coords[i+1]-1)*latScale));
   rect(Xcenter+(coords[i]*longScale*XaxisScale),Ycenter+(-coords[++i]-1)*latScale*YaxisScale,10*XaxisScale,10*YaxisScale);
  }
}


void testOne(){
  String[] lines = loadStrings("positions.txt");
  for(int i=0; i< lines.length;i++){
     String[] pieces = split(lines[i], ',');

     float x1 = Float.valueOf(pieces[0]).floatValue();
     float y1 = Float.valueOf(pieces[1]).floatValue();
     float x2 = Float.valueOf(pieces[2]).floatValue();
     float y2 = Float.valueOf(pieces[3]).floatValue();
     println("x1:"+x1+" <->"+"y1:"+y1+" <->"+"x2:"+x2+" <->"+"y2:"+y2);
     float[] point1 = translatePoint(x1,y1);
     float[] point2 = translatePoint(x2,y2);
     if(i==0){
       //println("x1:"+point1[0]+" <->"+"y1:"+point1[1]+" <->"+"x2:"+point2[0]+" <->"+"y2:"+point2[1]);
       line(point1[0],point1[1],point2[0],point2[1]);
     }
     else{
       println("=>  x1:"+point1[0]+" <->"+"y1:"+point1[1]+" <->"+"x2:"+point2[0]+" <->"+"y2:"+point2[1]);
       stroke(255,0,0);
       fill(255,0,0);
       ellipse(point1[0],point1[1],4,4);
       ellipse(point2[0],point2[1],4,4);
       //line(point1[0],point1[1],point2[0],point2[1]);
     }
  }
}

void testTwo(){
  float[] point1 = translatePoint(2,8);
  float[] point2 = translatePoint(58,108);
  line(point1[0],point1[1],point2[0],point2[1]);
  
  fill(255,0,0,100);
  noStroke();
  int[] coords = new int[]{0,0,0,2,1,4,2,5,3,7,4,9,5,10,4,8,5,9,3,6,4,7,3,5,1,3,2,4,0,1,1,2};
  pixelatedDraw(coords,10,10);
}

void testThree(){
String[] lines = loadStrings("test3.txt");
  for(int i=0; i< lines.length;i++){
     String[] pieces = split(lines[i], ',');

     float x1 = Float.valueOf(pieces[0]).floatValue();
     float y1 = Float.valueOf(pieces[1]).floatValue();
     float x2 = Float.valueOf(pieces[2]).floatValue();
     float y2 = Float.valueOf(pieces[3]).floatValue();
     println("x1:"+x1+" <->"+"y1:"+y1+" <->"+"x2:"+x2+" <->"+"y2:"+y2);
     float[] point1 = translatePoint(x1,y1);
     float[] point2 = translatePoint(x2,y2);
     if(i==0){
       //println("x1:"+point1[0]+" <->"+"y1:"+point1[1]+" <->"+"x2:"+point2[0]+" <->"+"y2:"+point2[1]);
       line(point1[0],point1[1],point2[0],point2[1]);
     }
     else{
       println("=>  x1:"+point1[0]+" <->"+"y1:"+point1[1]+" <->"+"x2:"+point2[0]+" <->"+"y2:"+point2[1]);
       stroke(255,0,0);
       fill(255,0,0);
       ellipse(point1[0],point1[1],4,4);
       ellipse(point2[0],point2[1],4,4);
       //line(point1[0],point1[1],point2[0],point2[1]);
     }
  }
}


