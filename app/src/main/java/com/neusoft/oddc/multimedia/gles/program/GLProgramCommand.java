package com.neusoft.oddc.multimedia.gles.program;

import com.neusoft.oddc.multimedia.gles.GlUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLProgramCommand {
    private static final String TAG = GLProgramCommand.class.getSimpleName();

    public enum DrawType {
        DRAW_ELEMENTS,
        DRAW_ARRAY
    }

    private DrawType drawType = DrawType.DRAW_ARRAY;

    private float[] mvpMatrix = GlUtil.createIdentityMatrix();
    private float[] texMatrix = GlUtil.createIdentityMatrix();

    private FloatBuffer vertexBuffer;
    private int firstVertexIndex;
    private int vertexCount;
    private int coordsPerVertex;
    private int vertexStride;
    private FloatBuffer textureCoordBuffer;
    private int[] textures;
    private int textureStride;
    private ShortBuffer indicesBuffer;
    private int indicesOffset;
    private int indicesCount;

    private FloatBuffer colorBuffer;
    private float[] color;

    public GLProgramCommand() {

    }

    public int getCoordsPerVertex() {
        return coordsPerVertex;
    }

    public void setCoordsPerVertex(int coordsPerVertex) {
        this.coordsPerVertex = coordsPerVertex;
    }

    public DrawType getDrawType() {
        return drawType;
    }

    public void setDrawType(DrawType drawType) {
        this.drawType = drawType;
    }

    public int getFirstVertexIndex() {
        return firstVertexIndex;
    }

    public void setFirstVertexIndex(int firstVertexIndex) {
        this.firstVertexIndex = firstVertexIndex;
    }

    public ShortBuffer getIndicesBuffer() {
        return indicesBuffer;
    }

    public void setIndicesBuffer(ShortBuffer indicesBuffer) {
        this.indicesBuffer = indicesBuffer;
    }

    public int getIndicesCount() {
        return indicesCount;
    }

    public void setIndicesCount(int indicesCount) {
        this.indicesCount = indicesCount;
    }

    public int getIndicesOffset() {
        return indicesOffset;
    }

    public void setIndicesOffset(int indicesOffset) {
        this.indicesOffset = indicesOffset;
    }

    public float[] getMvpMatrix() {
        return mvpMatrix;
    }

    public void setMvpMatrix(float[] mvpMatrix) {
        this.mvpMatrix = mvpMatrix;
    }

    public float[] getTexMatrix() {
        return texMatrix;
    }

    public void setTexMatrix(float[] texMatrix) {
        this.texMatrix = texMatrix;
    }

    public FloatBuffer getTextureCoordBuffer() {
        return textureCoordBuffer;
    }

    public void setTextureCoordBuffer(FloatBuffer textureCoordBuffer) {
        this.textureCoordBuffer = textureCoordBuffer;
    }

    public int[] getTextures() {
        return textures;
    }

    public void setTextures(int[] textures) {
        this.textures = textures;
    }

    public int getTextureStride() {
        return textureStride;
    }

    public void setTextureStride(int textureStride) {
        this.textureStride = textureStride;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(FloatBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    public int getVertexStride() {
        return vertexStride;
    }

    public void setVertexStride(int vertexStride) {
        this.vertexStride = vertexStride;
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public FloatBuffer getColorBuffer() {
        return colorBuffer;
    }

    public void setColorBuffer(FloatBuffer colorBuffer) {
        this.colorBuffer = colorBuffer;
    }
}
