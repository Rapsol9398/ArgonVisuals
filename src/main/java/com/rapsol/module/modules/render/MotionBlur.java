package com.rapsol.module.modules.render;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.NumberSetting;
import com.rapsol.utils.printString;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MotionBlur extends Module {
	private static final Identifier SCREENQUAD = Identifier.withDefaultNamespace("core/screenquad");
	private static final Identifier HISTORY = Identifier.fromNamespaceAndPath("argonvisuals", "motion_blur_history");
	private static final Identifier BLENDED = Identifier.fromNamespaceAndPath("argonvisuals", "motion_blur_blended");

	public final NumberSetting strength = new NumberSetting(printString.of("Strength"), 10, 90, 50, 1)
			.setDescription(printString.of("How much of the last frames stays visible"));

	private final Projection projection = new Projection();
	private ProjectionMatrixBuffer projectionBuffer;
	private PostChain chain;
	private double chainStrength;

	public MotionBlur() {
		super(
				printString.of("Motion Blur"),
				printString.of("Blurs the world while you move and look around"),
				-1,
				Category.RENDER
		);

		addSettings(strength);
		projection.setupOrtho(0.1f, 1000.0f, 1.0f, 1.0f, false);
	}

	@Override
	public void onDisable() {
		close();
		super.onDisable();
	}

	public void process(GraphicsResourceAllocator allocator) {
		if (projectionBuffer == null)
			projectionBuffer = new ProjectionMatrixBuffer("motion_blur");

		if (chain == null || strength.getValue() != chainStrength) {
			close();
			try {
				chain = PostChain.load(config(), mc.getTextureManager(), LevelTargetBundle.MAIN_TARGETS,
						Identifier.fromNamespaceAndPath("argonvisuals", "motion_blur"), projection, projectionBuffer);
				chainStrength = strength.getValue();
			} catch (ShaderManager.CompilationException e) {
				e.printStackTrace();
				setEnabled(false);
				return;
			}
		}

		chain.process(mc.getMainRenderTarget(), allocator);
	}

	private void close() {
		if (chain != null)
			chain.close();
		chain = null;
	}

	private PostChainConfig config() {
		PostChainConfig.Pass blend = new PostChainConfig.Pass(
				SCREENQUAD, Identifier.fromNamespaceAndPath("argonvisuals", "post/motion_blur"),
				List.of(
						new PostChainConfig.TargetInput("In", PostChain.MAIN_TARGET_ID, false, false),
						new PostChainConfig.TargetInput("Prev", HISTORY, false, false)
				),
				BLENDED,
				Map.of("MotionBlurConfig", List.of(new UniformValue.FloatUniform(strength.getValueFloat() / 100)))
		);

		return new PostChainConfig(
				Map.of(
						HISTORY, new PostChainConfig.InternalTarget(Optional.empty(), Optional.empty(), true, 0),
						BLENDED, new PostChainConfig.InternalTarget(Optional.empty(), Optional.empty(), false, 0)
				),
				List.of(blend, blit(BLENDED, HISTORY), blit(BLENDED, PostChain.MAIN_TARGET_ID))
		);
	}

	private static PostChainConfig.Pass blit(Identifier from, Identifier to) {
		return new PostChainConfig.Pass(
				SCREENQUAD, Identifier.withDefaultNamespace("post/blit"),
				List.of(new PostChainConfig.TargetInput("In", from, false, false)),
				to,
				Map.of("BlitConfig", List.of(new UniformValue.Vec4Uniform(new Vector4f(1, 1, 1, 1))))
		);
	}
}
