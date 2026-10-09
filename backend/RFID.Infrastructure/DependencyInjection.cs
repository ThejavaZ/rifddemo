using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using RFID.Application.Ports;
using RFID.Domain.Ports;
using RFID.Infrastructure.Auth;
using RFID.Infrastructure.Persistence;
using RFID.Infrastructure.Persistence.Repositories;
using RFID.Infrastructure.Seeding;

namespace RFID.Infrastructure;

public static class DependencyInjection
{
    // Conecta los puertos (Domain/Application) con sus adaptadores concretos (EF Core, BCrypt, JWT).
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        var connectionString = configuration.GetConnectionString("Database")
            ?? throw new InvalidOperationException("ConnectionStrings:Database es requerido");

        services.AddDbContext<AppDbContext>(options => options.AddRfidDatabase(connectionString));

        services.AddScoped<ITagRepository, EfTagRepository>();
        services.AddScoped<IInventoryRepository, EfInventoryRepository>();
        services.AddScoped<IUserRepository, EfUserRepository>();
        services.AddScoped<IPasswordHasher, BcryptPasswordHasher>();
        services.AddScoped<ITokenService, JwtTokenService>();
        services.AddScoped<IDataSeeder, InventorySeeder>();
        services.AddScoped<IDataSeeder, UserSeeder>();

        return services;
    }

    // Único punto donde se configura Npgsql + snake_case: runtime y design-time comparten el mismo modelo.
    public static DbContextOptionsBuilder AddRfidDatabase(this DbContextOptionsBuilder options, string connectionString)
    {
        options.UseNpgsql(connectionString);
        options.UseSnakeCaseNamingConvention();
        return options;
    }
}
